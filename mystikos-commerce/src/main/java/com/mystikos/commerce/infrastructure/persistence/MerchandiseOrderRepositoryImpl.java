package com.mystikos.commerce.infrastructure.persistence;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.mystikos.commerce.domain.model.MerchandiseOrder;
import com.mystikos.commerce.domain.model.OrderLineItem;
import com.mystikos.commerce.domain.model.OrderStatus;
import com.mystikos.commerce.domain.repository.MerchandiseOrderRepository;
import com.mystikos.common.result.PageResult;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/** 订单聚合横跨 commerce_order + commerce_order_item 两张表，仓储实现里一起维护。 */
@Repository
public class MerchandiseOrderRepositoryImpl implements MerchandiseOrderRepository {

    private final MerchandiseOrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;

    public MerchandiseOrderRepositoryImpl(MerchandiseOrderMapper orderMapper, OrderItemMapper orderItemMapper) {
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
    }

    @Override
    public Optional<MerchandiseOrder> findById(Long id) {
        MerchandiseOrderPO orderPO = orderMapper.selectById(id);
        if (orderPO == null) {
            return Optional.empty();
        }
        List<OrderItemPO> itemPOs = orderItemMapper.selectList(
                Wrappers.<OrderItemPO>lambdaQuery().eq(OrderItemPO::getOrderId, id));
        return Optional.of(toDomain(orderPO, itemPOs));
    }

    @Override
    public MerchandiseOrder save(MerchandiseOrder order) {
        MerchandiseOrderPO orderPO = toPO(order);
        if (orderPO.getId() == null) {
            orderMapper.insert(orderPO);
            order.assignId(orderPO.getId());
            for (OrderLineItem item : order.getItems()) {
                OrderItemPO itemPO = new OrderItemPO();
                itemPO.setOrderId(order.getId());
                itemPO.setProductId(item.productId());
                itemPO.setProductNameSnapshot(item.productNameSnapshot());
                itemPO.setUnitPriceSnapshot(item.unitPriceSnapshot());
                itemPO.setQuantity(item.quantity());
                orderItemMapper.insert(itemPO);
            }
        } else {
            orderMapper.updateById(orderPO);
        }
        return order;
    }

    @Override
    public PageResult<MerchandiseOrder> findByPatronId(Long patronId, int pageNum, int pageSize) {
        return findPage(null, patronId, pageNum, pageSize);
    }

    @Override
    public PageResult<MerchandiseOrder> findPage(OrderStatus status, Long patronId, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<MerchandiseOrderPO> orderPOs = orderMapper.selectList(Wrappers.<MerchandiseOrderPO>lambdaQuery()
                .eq(patronId != null, MerchandiseOrderPO::getPatronId, patronId)
                .eq(status != null, MerchandiseOrderPO::getStatus, status == null ? null : status.name())
                .orderByDesc(MerchandiseOrderPO::getCreatedAt));
        PageInfo<MerchandiseOrderPO> pageInfo = new PageInfo<>(orderPOs);
        if (orderPOs.isEmpty()) {
            return PageResult.of(List.of(), pageInfo.getTotal(), pageNum, pageSize);
        }

        List<Long> orderIds = orderPOs.stream().map(MerchandiseOrderPO::getId).toList();
        Map<Long, List<OrderItemPO>> itemsByOrderId = orderItemMapper.selectList(
                        Wrappers.<OrderItemPO>lambdaQuery().in(OrderItemPO::getOrderId, orderIds))
                .stream().collect(Collectors.groupingBy(OrderItemPO::getOrderId));

        List<MerchandiseOrder> orders = orderPOs.stream()
                .map(po -> toDomain(po, itemsByOrderId.getOrDefault(po.getId(), List.of())))
                .toList();
        return PageResult.of(orders, pageInfo.getTotal(), pageNum, pageSize);
    }

    private MerchandiseOrderPO toPO(MerchandiseOrder order) {
        MerchandiseOrderPO po = new MerchandiseOrderPO();
        po.setId(order.getId());
        po.setPatronId(order.getPatronId());
        po.setTotalAmount(order.getTotalAmount());
        po.setShippingAddress(order.getShippingAddress());
        po.setStatus(order.getStatus().name());
        po.setCreatedAt(order.getCreatedAt());
        return po;
    }

    private MerchandiseOrder toDomain(MerchandiseOrderPO orderPO, List<OrderItemPO> itemPOs) {
        List<OrderLineItem> items = itemPOs.stream()
                .map(po -> new OrderLineItem(po.getProductId(), po.getProductNameSnapshot(),
                        po.getUnitPriceSnapshot(), po.getQuantity()))
                .toList();
        return MerchandiseOrder.restore(orderPO.getId(), orderPO.getPatronId(), items, orderPO.getTotalAmount(),
                orderPO.getShippingAddress(), OrderStatus.valueOf(orderPO.getStatus()), orderPO.getCreatedAt());
    }
}
