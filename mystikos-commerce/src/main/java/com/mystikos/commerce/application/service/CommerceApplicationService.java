package com.mystikos.commerce.application.service;

import com.mystikos.commerce.application.command.AddToCartCommand;
import com.mystikos.commerce.application.command.CreateOrderCommand;
import com.mystikos.commerce.application.port.PaymentCheckoutResult;
import com.mystikos.commerce.application.port.PaymentPort;
import com.mystikos.commerce.domain.CommerceException;
import com.mystikos.commerce.domain.event.OrderPlacedEvent;
import com.mystikos.commerce.domain.model.CartItem;
import com.mystikos.commerce.domain.model.InventoryStock;
import com.mystikos.commerce.domain.model.MerchandiseOrder;
import com.mystikos.commerce.domain.model.OrderLineItem;
import com.mystikos.commerce.domain.model.OrderStatus;
import com.mystikos.commerce.domain.model.Product;
import com.mystikos.commerce.domain.model.WishlistItem;
import com.mystikos.commerce.domain.repository.CartItemRepository;
import com.mystikos.commerce.domain.repository.InventoryStockRepository;
import com.mystikos.commerce.domain.repository.MerchandiseOrderRepository;
import com.mystikos.commerce.domain.repository.ProductRepository;
import com.mystikos.commerce.domain.repository.WishlistItemRepository;
import com.mystikos.common.event.DomainEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CommerceApplicationService {

    /**
     * 结算币种暂时固定为欧元——MerchandiseOrder 聚合目前没有按订单存币种的字段，
     * 多币种支持留给后续。
     */
    private static final String DEFAULT_CURRENCY = "EUR";

    private final ProductRepository productRepository;
    private final CartItemRepository cartItemRepository;
    private final WishlistItemRepository wishlistItemRepository;
    private final InventoryStockRepository inventoryStockRepository;
    private final MerchandiseOrderRepository merchandiseOrderRepository;
    private final DomainEventPublisher eventPublisher;
    private final PaymentPort paymentPort;

    public CommerceApplicationService(ProductRepository productRepository,
                                       CartItemRepository cartItemRepository,
                                       WishlistItemRepository wishlistItemRepository,
                                       InventoryStockRepository inventoryStockRepository,
                                       MerchandiseOrderRepository merchandiseOrderRepository,
                                       DomainEventPublisher eventPublisher,
                                       PaymentPort paymentPort) {
        this.productRepository = productRepository;
        this.cartItemRepository = cartItemRepository;
        this.wishlistItemRepository = wishlistItemRepository;
        this.inventoryStockRepository = inventoryStockRepository;
        this.merchandiseOrderRepository = merchandiseOrderRepository;
        this.eventPublisher = eventPublisher;
        this.paymentPort = paymentPort;
    }

    public List<Product> listProducts() {
        return productRepository.findAllOnShelf();
    }

    public Product getProduct(Long productId) {
        return productRepository.findById(productId).orElseThrow(() -> CommerceException.productNotFound(productId));
    }

    @Transactional
    public void addToCart(AddToCartCommand command) {
        Product product = requireOnShelf(command.productId());
        CartItem existing = cartItemRepository.findByPatronAndProduct(command.patronId(), product.getId())
                .orElse(null);
        if (existing == null) {
            cartItemRepository.save(CartItem.create(command.patronId(), product.getId(), command.quantity()));
        } else {
            existing.increaseQuantity(command.quantity());
            cartItemRepository.save(existing);
        }
    }

    @Transactional
    public void removeFromCart(Long patronId, Long productId) {
        cartItemRepository.deleteByPatronAndProduct(patronId, productId);
    }

    public List<CartLineView> getCart(Long patronId) {
        return cartItemRepository.findAllByPatron(patronId).stream()
                .map(item -> {
                    Product product = productRepository.findById(item.getProductId())
                            .orElseThrow(() -> CommerceException.productNotFound(item.getProductId()));
                    return new CartLineView(product.getId(), product.getName(), product.getPrice(),
                            item.getQuantity(), product.getPrice().multiply(java.math.BigDecimal.valueOf(item.getQuantity())));
                })
                .toList();
    }

    @Transactional
    public void addToWishlist(Long patronId, Long productId) {
        requireOnShelf(productId);
        if (wishlistItemRepository.findByPatronAndProduct(patronId, productId).isEmpty()) {
            wishlistItemRepository.save(WishlistItem.create(patronId, productId));
        }
    }

    @Transactional
    public void removeFromWishlist(Long patronId, Long productId) {
        wishlistItemRepository.deleteByPatronAndProduct(patronId, productId);
    }

    public List<WishlistLineView> getWishlist(Long patronId) {
        return wishlistItemRepository.findAllByPatron(patronId).stream()
                .map(item -> {
                    Product product = productRepository.findById(item.getProductId())
                            .orElseThrow(() -> CommerceException.productNotFound(item.getProductId()));
                    return new WishlistLineView(product.getId(), product.getName(), product.getPrice(), item.getAddedAt());
                })
                .toList();
    }

    /** 用当前购物车内容下单：逐行校验商品在架、预占库存，成功后清空购物车。任何一行库存不足就整单失败。 */
    @Transactional
    public Long createOrder(CreateOrderCommand command) {
        List<CartItem> cartItems = cartItemRepository.findAllByPatron(command.patronId());
        if (cartItems.isEmpty()) {
            throw CommerceException.orderEmpty();
        }

        List<OrderLineItem> lineItems = cartItems.stream()
                .map(cartItem -> {
                    Product product = requireOnShelf(cartItem.getProductId());
                    InventoryStock stock = inventoryStockRepository.findByProductId(product.getId())
                            .orElseThrow(() -> CommerceException.insufficientStock(product.getId()));
                    stock.reserve(cartItem.getQuantity());
                    inventoryStockRepository.save(stock);
                    return new OrderLineItem(product.getId(), product.getName(), product.getPrice(), cartItem.getQuantity());
                })
                .toList();

        MerchandiseOrder order = MerchandiseOrder.create(command.patronId(), lineItems, command.shippingAddress());
        MerchandiseOrder saved = merchandiseOrderRepository.save(order);
        cartItemRepository.deleteAllByPatron(command.patronId());

        eventPublisher.publish(new OrderPlacedEvent(saved.getId(), saved.getPatronId(), saved.getTotalAmount()));
        return saved.getId();
    }

    @Transactional
    public void cancelOrder(Long patronId, Long orderId) {
        MerchandiseOrder order = merchandiseOrderRepository.findById(orderId)
                .orElseThrow(() -> CommerceException.orderNotFound(orderId));
        if (!order.getPatronId().equals(patronId)) {
            throw CommerceException.orderNotFound(orderId);
        }
        order.cancel();
        merchandiseOrderRepository.save(order);

        for (OrderLineItem item : order.getItems()) {
            inventoryStockRepository.findByProductId(item.productId()).ifPresent(stock -> {
                stock.release(item.quantity());
                inventoryStockRepository.save(stock);
            });
        }
    }

    /** 发起结账：把订单转 PENDING_PAYMENT，返回前端完成支付所需的 clientSecret。 */
    @Transactional
    public PaymentCheckoutResult requestPayment(Long orderId, Long patronId) {
        MerchandiseOrder order = merchandiseOrderRepository.findById(orderId)
                .orElseThrow(() -> CommerceException.orderNotFound(orderId));
        if (!order.getPatronId().equals(patronId)) {
            throw CommerceException.orderNotFound(orderId);
        }
        PaymentCheckoutResult checkout = paymentPort.requestPayment(
                order.getId(), order.getPatronId(), order.getTotalAmount(), DEFAULT_CURRENCY);
        // 重复调用本接口时 PaymentPort 会复用同一个未终态 intent，订单这边也只在还是 DRAFT 时迁移一次。
        if (order.getStatus() == OrderStatus.DRAFT) {
            order.requestPayment();
            merchandiseOrderRepository.save(order);
        }
        return checkout;
    }

    /** 由 PaymentCapturedEventListener 在支付成功后调用，把订单推进到 PAID。 */
    @Transactional
    public void markPaid(Long orderId) {
        MerchandiseOrder order = merchandiseOrderRepository.findById(orderId)
                .orElseThrow(() -> CommerceException.orderNotFound(orderId));
        if (order.getStatus() == OrderStatus.PAID) {
            return;
        }
        order.markPaid();
        merchandiseOrderRepository.save(order);
    }

    public OrderView getOrder(Long orderId) {
        MerchandiseOrder order = merchandiseOrderRepository.findById(orderId)
                .orElseThrow(() -> CommerceException.orderNotFound(orderId));
        return new OrderView(order.getId(), order.getPatronId(), order.getItems(), order.getTotalAmount(),
                order.getShippingAddress(), order.getStatus(), order.getCreatedAt());
    }

    private Product requireOnShelf(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> CommerceException.productNotFound(productId));
        if (!product.isOnShelf()) {
            throw CommerceException.productOffShelf(productId);
        }
        return product;
    }
}
