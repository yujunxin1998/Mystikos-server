package com.mystikos.commerce.application.service;

import com.mystikos.commerce.application.command.CreateDirectOrderCommand;
import com.mystikos.commerce.application.command.CreateOrderCommand;
import com.mystikos.commerce.application.port.PaymentPort;
import com.mystikos.commerce.domain.CommerceException;
import com.mystikos.commerce.domain.model.AddressType;
import com.mystikos.commerce.domain.model.CartItem;
import com.mystikos.commerce.domain.model.InventoryStock;
import com.mystikos.commerce.domain.model.MerchandiseOrder;
import com.mystikos.commerce.domain.model.OrderLineItem;
import com.mystikos.commerce.domain.model.OrderStatus;
import com.mystikos.commerce.domain.model.PatronAddress;
import com.mystikos.commerce.domain.model.Product;
import com.mystikos.commerce.domain.repository.CartItemRepository;
import com.mystikos.commerce.domain.repository.InventoryStockRepository;
import com.mystikos.commerce.domain.repository.MerchandiseOrderRepository;
import com.mystikos.commerce.domain.repository.PatronAddressRepository;
import com.mystikos.commerce.domain.repository.ProductRepository;
import com.mystikos.commerce.domain.repository.WishlistItemRepository;
import com.mystikos.common.event.DomainEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 纯 Mockito 单元测试，不起 Spring 容器——覆盖购物车部分选购结算、立即购买跳过购物车、
 * 订单懒过期同步+释放库存这几条这次新加的行为，同 mystikos-identity 现有测试的风格。
 */
class CommerceOrderCheckoutTest {

    private ProductRepository productRepository;
    private CartItemRepository cartItemRepository;
    private InventoryStockRepository inventoryStockRepository;
    private MerchandiseOrderRepository merchandiseOrderRepository;
    private PatronAddressRepository patronAddressRepository;
    private DomainEventPublisher eventPublisher;
    private CommerceApplicationService service;

    private static final Long PATRON_ID = 1001L;
    private static final Long ADDRESS_ID = 5L;

    @BeforeEach
    void setUp() {
        productRepository = mock(ProductRepository.class);
        cartItemRepository = mock(CartItemRepository.class);
        WishlistItemRepository wishlistItemRepository = mock(WishlistItemRepository.class);
        inventoryStockRepository = mock(InventoryStockRepository.class);
        merchandiseOrderRepository = mock(MerchandiseOrderRepository.class);
        patronAddressRepository = mock(PatronAddressRepository.class);
        eventPublisher = mock(DomainEventPublisher.class);
        PaymentPort paymentPort = mock(PaymentPort.class);

        service = new CommerceApplicationService(productRepository, cartItemRepository, wishlistItemRepository,
                inventoryStockRepository, merchandiseOrderRepository, patronAddressRepository, eventPublisher,
                paymentPort);

        PatronAddress address = PatronAddress.create(PATRON_ID, AddressType.OVERSEAS, "Jane Doe", "+49123456789",
                "DE", null, "Berlin", null, "Musterstr. 1", null, null, null, false);
        address.assignId(ADDRESS_ID);
        when(patronAddressRepository.findById(ADDRESS_ID)).thenReturn(Optional.of(address));

        AtomicLong orderIdSequence = new AtomicLong(100);
        when(merchandiseOrderRepository.save(any())).thenAnswer(invocation -> {
            MerchandiseOrder order = invocation.getArgument(0);
            if (order.getId() == null) {
                order.assignId(orderIdSequence.getAndIncrement());
            }
            return order;
        });
    }

    @Test
    void partialCartSelectionOnlyOrdersSelectedLinesAndKeepsRestInCart() {
        onShelfProductWithStock(1L, "T恤", "89.00", 10);
        onShelfProductWithStock(2L, "马克杯", "39.00", 10);
        onShelfProductWithStock(3L, "钥匙扣", "199.00", 10);
        when(cartItemRepository.findAllByPatron(PATRON_ID)).thenReturn(List.of(
                CartItem.restore(11L, PATRON_ID, 1L, 2),
                CartItem.restore(12L, PATRON_ID, 2L, 1),
                CartItem.restore(13L, PATRON_ID, 3L, 1)));

        Long orderId = service.createOrder(new CreateOrderCommand(PATRON_ID, List.of(1L, 3L), ADDRESS_ID));

        assertThat(orderId).isNotNull();
        ArgumentCaptor<MerchandiseOrder> orderCaptor = ArgumentCaptor.forClass(MerchandiseOrder.class);
        verify(merchandiseOrderRepository).save(orderCaptor.capture());
        List<Long> orderedProductIds = orderCaptor.getValue().getItems().stream().map(OrderLineItem::productId).toList();
        assertThat(orderedProductIds).containsExactlyInAnyOrder(1L, 3L);

        // 只清掉选中的两行，商品2继续留在购物车——不是老版本"整车清空"的行为。
        verify(cartItemRepository).deleteByPatronAndProducts(PATRON_ID, List.of(1L, 3L));
        verify(cartItemRepository, never()).deleteAllByPatron(any());
    }

    @Test
    void selectingProductNotInCartThrowsCartLineNotFound() {
        when(cartItemRepository.findAllByPatron(PATRON_ID)).thenReturn(
                List.of(CartItem.restore(11L, PATRON_ID, 1L, 1)));

        assertThatThrownBy(() -> service.createOrder(new CreateOrderCommand(PATRON_ID, List.of(1L, 99L), ADDRESS_ID)))
                .isInstanceOf(CommerceException.class)
                .hasMessageContaining("99");
    }

    @Test
    void directOrderNeverTouchesCart() {
        onShelfProductWithStock(1L, "T恤", "89.00", 10);

        Long orderId = service.createDirectOrder(new CreateDirectOrderCommand(PATRON_ID, 1L, 2, ADDRESS_ID));

        assertThat(orderId).isNotNull();
        verify(cartItemRepository, never()).findAllByPatron(any());
        verify(cartItemRepository, never()).deleteByPatronAndProducts(any(), anyList());
        verify(cartItemRepository, never()).deleteAllByPatron(any());
    }

    @Test
    void gettingOrderPastPaymentValidityLazilyExpiresItAndReleasesInventory() {
        OrderLineItem line = new OrderLineItem(1L, "T恤", new BigDecimal("89.00"), 2);
        OffsetDateTime staleCreatedAt = OffsetDateTime.now().minus(MerchandiseOrder.PAYMENT_VALIDITY).minusMinutes(1);
        MerchandiseOrder overdueOrder = MerchandiseOrder.restore(200L, PATRON_ID, List.of(line),
                new BigDecimal("178.00"), "snapshot address", ADDRESS_ID, OrderStatus.PENDING_PAYMENT, staleCreatedAt);
        when(merchandiseOrderRepository.findById(200L)).thenReturn(Optional.of(overdueOrder));
        InventoryStock stock = InventoryStock.restore(1L, 8, 2);
        when(inventoryStockRepository.findByProductId(1L)).thenReturn(Optional.of(stock));

        OrderView view = service.getOrder(200L, PATRON_ID);

        assertThat(view.status()).isEqualTo(OrderStatus.EXPIRED);
        verify(inventoryStockRepository).save(eq(stock));
        assertThat(stock.getAvailableQty()).isEqualTo(10);
        assertThat(stock.getReservedQty()).isEqualTo(0);
    }

    @Test
    void expireOverdueOrdersBatchExpiresAndReleasesInventoryForEach() {
        OrderLineItem lineA = new OrderLineItem(1L, "T恤", new BigDecimal("89.00"), 1);
        OrderLineItem lineB = new OrderLineItem(2L, "马克杯", new BigDecimal("39.00"), 1);
        OffsetDateTime staleCreatedAt = OffsetDateTime.now().minus(MerchandiseOrder.PAYMENT_VALIDITY).minusMinutes(1);
        MerchandiseOrder orderA = MerchandiseOrder.restore(201L, PATRON_ID, List.of(lineA), new BigDecimal("89.00"),
                "addr", ADDRESS_ID, OrderStatus.DRAFT, staleCreatedAt);
        MerchandiseOrder orderB = MerchandiseOrder.restore(202L, PATRON_ID, List.of(lineB), new BigDecimal("39.00"),
                "addr", ADDRESS_ID, OrderStatus.PENDING_PAYMENT, staleCreatedAt);
        when(merchandiseOrderRepository.findExpirable(any())).thenReturn(List.of(orderA, orderB));
        when(inventoryStockRepository.findByProductId(1L)).thenReturn(Optional.of(InventoryStock.restore(1L, 5, 1)));
        when(inventoryStockRepository.findByProductId(2L)).thenReturn(Optional.of(InventoryStock.restore(2L, 5, 1)));

        service.expireOverdueOrders();

        assertThat(orderA.getStatus()).isEqualTo(OrderStatus.EXPIRED);
        assertThat(orderB.getStatus()).isEqualTo(OrderStatus.EXPIRED);
        verify(merchandiseOrderRepository).save(orderA);
        verify(merchandiseOrderRepository).save(orderB);
    }

    private void onShelfProductWithStock(Long productId, String name, String price, int availableQty) {
        Product product = Product.restore(productId, 1L, name, "desc", new BigDecimal(price), List.of(),
                com.mystikos.commerce.domain.model.ProductStatus.ON_SHELF);
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(inventoryStockRepository.findByProductId(productId))
                .thenReturn(Optional.of(InventoryStock.restore(productId, availableQty, 0)));
    }
}
