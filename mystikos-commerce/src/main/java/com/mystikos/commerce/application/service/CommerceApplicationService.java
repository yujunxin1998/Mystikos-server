package com.mystikos.commerce.application.service;

import com.mystikos.commerce.application.command.AddToCartCommand;
import com.mystikos.commerce.application.command.CreateOrderCommand;
import com.mystikos.commerce.domain.CommerceException;
import com.mystikos.commerce.domain.event.OrderPlacedEvent;
import com.mystikos.commerce.domain.model.CartItem;
import com.mystikos.commerce.domain.model.InventoryStock;
import com.mystikos.commerce.domain.model.MerchandiseOrder;
import com.mystikos.commerce.domain.model.OrderLineItem;
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

    private final ProductRepository productRepository;
    private final CartItemRepository cartItemRepository;
    private final WishlistItemRepository wishlistItemRepository;
    private final InventoryStockRepository inventoryStockRepository;
    private final MerchandiseOrderRepository merchandiseOrderRepository;
    private final DomainEventPublisher eventPublisher;

    public CommerceApplicationService(ProductRepository productRepository,
                                       CartItemRepository cartItemRepository,
                                       WishlistItemRepository wishlistItemRepository,
                                       InventoryStockRepository inventoryStockRepository,
                                       MerchandiseOrderRepository merchandiseOrderRepository,
                                       DomainEventPublisher eventPublisher) {
        this.productRepository = productRepository;
        this.cartItemRepository = cartItemRepository;
        this.wishlistItemRepository = wishlistItemRepository;
        this.inventoryStockRepository = inventoryStockRepository;
        this.merchandiseOrderRepository = merchandiseOrderRepository;
        this.eventPublisher = eventPublisher;
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
