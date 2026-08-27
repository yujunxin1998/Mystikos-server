package com.mystikos.commerce.application.service;

import com.mystikos.commerce.application.command.AddToCartCommand;
import com.mystikos.commerce.application.command.CreateOrderCommand;
import com.mystikos.commerce.application.command.CreateProductCommand;
import com.mystikos.commerce.application.command.UpdateProductCommand;
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
import com.mystikos.commerce.domain.model.ProductStatus;
import com.mystikos.commerce.domain.model.WishlistItem;
import com.mystikos.commerce.domain.repository.CartItemRepository;
import com.mystikos.commerce.domain.repository.InventoryStockRepository;
import com.mystikos.commerce.domain.repository.MerchandiseOrderRepository;
import com.mystikos.commerce.domain.repository.ProductRepository;
import com.mystikos.commerce.domain.repository.WishlistItemRepository;
import com.mystikos.common.event.DomainEventPublisher;
import com.mystikos.common.result.PageResult;
import com.mystikos.payment.application.port.PaymentScene;
import com.mystikos.payment.domain.model.PaymentProvider;
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

    /** 后台分页查询商品，不限上下架状态；status 非空则按状态过滤。 */
    public PageResult<Product> listProductsForAdmin(ProductStatus status, int pageNum, int pageSize) {
        return productRepository.findPage(status, pageNum, pageSize);
    }

    /** 后台编辑商品：整体覆盖式更新基础信息，status 不传则保持原状态。不涉及库存调整。 */
    @Transactional
    public Product updateProduct(Long productId, UpdateProductCommand command) {
        Product product = getProduct(productId);
        product.updateDetails(command.categoryId(), command.name(), command.description(),
                command.price(), command.images());
        if (command.status() != null) {
            product.changeStatus(command.status());
        }
        return productRepository.save(product);
    }

    /** 后台新增商品：落库后一并初始化库存行，产品创建即可下单，不需要再单独一步配置库存。 */
    @Transactional
    public Long createProduct(CreateProductCommand command) {
        Product product = Product.create(command.categoryId(), command.name(), command.description(),
                command.price(), command.images());
        Product saved = productRepository.save(product);
        inventoryStockRepository.insert(InventoryStock.create(saved.getId(), command.initialStock()));
        return saved.getId();
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
        MerchandiseOrder order = requireOrder(orderId);
        if (!order.getPatronId().equals(patronId)) {
            throw CommerceException.orderNotFound(orderId);
        }
        order.cancel();
        merchandiseOrderRepository.save(order);
        releaseInventory(order);
    }

    /**
     * 发起结账：把订单转 PENDING_PAYMENT，返回前端完成支付所需的 payload。
     * 结算币种固定欧元（见类注释），选支付宝/微信时网关会因为币种不是 CNY 直接拒绝——
     * 在给这两个订单类型接入人民币定价之前，这是预期内的限制，不是 bug。
     */
    @Transactional
    public PaymentCheckoutResult requestPayment(Long orderId, Long patronId, PaymentProvider provider, PaymentScene scene) {
        MerchandiseOrder order = requireOrder(orderId);
        if (!order.getPatronId().equals(patronId)) {
            throw CommerceException.orderNotFound(orderId);
        }
        PaymentCheckoutResult checkout = paymentPort.requestPayment(
                order.getId(), order.getPatronId(), order.getTotalAmount(), DEFAULT_CURRENCY, provider, scene);
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
        MerchandiseOrder order = requireOrder(orderId);
        if (order.getStatus() == OrderStatus.PAID) {
            return;
        }
        order.markPaid();
        merchandiseOrderRepository.save(order);
    }

    public OrderView getOrder(Long orderId) {
        return toOrderView(requireOrder(orderId));
    }

    /** 我的商品订单列表，按下单时间倒序分页。 */
    public PageResult<OrderView> listMyOrders(Long patronId, int pageNum, int pageSize) {
        PageResult<MerchandiseOrder> page = merchandiseOrderRepository.findByPatronId(patronId, pageNum, pageSize);
        List<OrderView> views = page.records().stream().map(this::toOrderView).toList();
        return PageResult.of(views, page.total(), page.pageNum(), page.pageSize());
    }

    /** 后台分页查询订单，不限买家，可按状态/买家ID过滤。 */
    public PageResult<OrderView> listOrdersForAdmin(OrderStatus status, Long patronId, int pageNum, int pageSize) {
        PageResult<MerchandiseOrder> page = merchandiseOrderRepository.findPage(status, patronId, pageNum, pageSize);
        List<OrderView> views = page.records().stream().map(this::toOrderView).toList();
        return PageResult.of(views, page.total(), page.pageNum(), page.pageSize());
    }

    /** 后台推进订单：PAID → FULFILLING。 */
    @Transactional
    public OrderView startFulfillingOrder(Long orderId) {
        MerchandiseOrder order = requireOrder(orderId);
        order.startFulfilling();
        return toOrderView(merchandiseOrderRepository.save(order));
    }

    /** 后台推进订单：FULFILLING → SHIPPED。 */
    @Transactional
    public OrderView shipOrder(Long orderId) {
        MerchandiseOrder order = requireOrder(orderId);
        order.ship();
        return toOrderView(merchandiseOrderRepository.save(order));
    }

    /** 后台推进订单：SHIPPED → COMPLETED。 */
    @Transactional
    public OrderView completeOrder(Long orderId) {
        MerchandiseOrder order = requireOrder(orderId);
        order.complete();
        return toOrderView(merchandiseOrderRepository.save(order));
    }

    /** 后台取消订单：不限买家本人，DRAFT/PENDING_PAYMENT/PAID 状态才允许，取消后释放预占库存。 */
    @Transactional
    public OrderView adminCancelOrder(Long orderId) {
        MerchandiseOrder order = requireOrder(orderId);
        order.cancel();
        MerchandiseOrder saved = merchandiseOrderRepository.save(order);
        releaseInventory(saved);
        return toOrderView(saved);
    }

    /**
     * 后台退款：PAID/FULFILLING/SHIPPED/COMPLETED 状态才允许。和取消一样把预占库存放回可售——
     * 聚合暂无"已发货、库存不可退回"的更细状态区分，先按可退货处理。
     */
    @Transactional
    public OrderView refundOrder(Long orderId) {
        MerchandiseOrder order = requireOrder(orderId);
        order.refund();
        MerchandiseOrder saved = merchandiseOrderRepository.save(order);
        releaseInventory(saved);
        return toOrderView(saved);
    }

    private Product requireOnShelf(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> CommerceException.productNotFound(productId));
        if (!product.isOnShelf()) {
            throw CommerceException.productOffShelf(productId);
        }
        return product;
    }

    private MerchandiseOrder requireOrder(Long orderId) {
        return merchandiseOrderRepository.findById(orderId)
                .orElseThrow(() -> CommerceException.orderNotFound(orderId));
    }

    private OrderView toOrderView(MerchandiseOrder order) {
        return new OrderView(order.getId(), order.getPatronId(), order.getItems(), order.getTotalAmount(),
                order.getShippingAddress(), order.getStatus(), order.getCreatedAt());
    }

    private void releaseInventory(MerchandiseOrder order) {
        for (OrderLineItem item : order.getItems()) {
            inventoryStockRepository.findByProductId(item.productId()).ifPresent(stock -> {
                stock.release(item.quantity());
                inventoryStockRepository.save(stock);
            });
        }
    }
}
