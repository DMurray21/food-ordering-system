package com.food.ordering.system.order.service.data.access.order.adapter;

import com.food.ordering.system.order.service.data.access.order.entity.OrderEntity;
import com.food.ordering.system.order.service.data.access.order.mapper.OrderDataAccessMapper;
import com.food.ordering.system.order.service.data.access.order.repository.OrderJpaRepository;
import com.food.ordering.system.order.service.domain.entity.Order;
import com.food.ordering.system.order.service.domain.valueobject.TrackingId;
import com.food.ordering.system.service.domain.ports.output.repository.OrderRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class OrderRepositoryImpl implements OrderRepository {

    private final OrderJpaRepository orderJpaRepository;
    private final OrderDataAccessMapper orderDataAccessMapper;

    public OrderRepositoryImpl(OrderJpaRepository orderJpaRepository, OrderDataAccessMapper orderDataAccessMapper) {
        this.orderJpaRepository = orderJpaRepository;
        this.orderDataAccessMapper = orderDataAccessMapper;
    }

    @Override
    public Order save(Order order) {
        OrderEntity saved = orderJpaRepository.save(orderDataAccessMapper.orderToOrderEntity(order));
        return orderDataAccessMapper.orderEntityToOrder(saved);
    }

    @Override
    public Optional<Order> findByTrackingId(TrackingId trackingId) {
        return orderJpaRepository.findByTrackingId(trackingId.getValue())
                .map(orderDataAccessMapper::orderEntityToOrder);
    }
}
