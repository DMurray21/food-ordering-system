package com.food.ordering.system.service.domain;

import com.food.ordering.system.domain.event.EmptyEvent;
import com.food.ordering.system.order.service.domain.OrderDomainService;
import com.food.ordering.system.order.service.domain.entity.Order;
import com.food.ordering.system.order.service.domain.event.OrderPaidEvent;
import com.food.ordering.system.saga.SagaStep;
import com.food.ordering.system.service.domain.dto.message.PaymentResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
public class OrderPaymentSaga implements SagaStep<PaymentResponse, OrderPaidEvent, EmptyEvent> {

    private final OrderDomainService orderDomainService;
    private final OrderPaidRestaurantRequestMessagePublisher paidRestaurantRequestMessagePublisher;
    private final OrderSagaHelper orderSagaHelper;

    public OrderPaymentSaga(OrderDomainService orderDomainService, OrderPaidRestaurantRequestMessagePublisher paidRestaurantRequestMessagePublisher, OrderSagaHelper orderSagaHelper) {
        this.orderDomainService = orderDomainService;
        this.paidRestaurantRequestMessagePublisher = paidRestaurantRequestMessagePublisher;
        this.orderSagaHelper = orderSagaHelper;
    }

    @Override
    @Transactional
    public OrderPaidEvent process(PaymentResponse data) {
        log.info("Completing payment for order with id: {}", data.getOrderId());
        Order order = orderSagaHelper.findOrder(data.getOrderId());
        OrderPaidEvent orderPaidEvent = orderDomainService.payOrder(order, paidRestaurantRequestMessagePublisher);
        orderSagaHelper.saveOrder(order);
        log.info("Order with id: {} is paid", data.getOrderId());
        return orderPaidEvent;
    }

    @Override
    @Transactional
    public EmptyEvent rollback(PaymentResponse data) {
        log.info("Cancelling order with id {}", data.getOrderId());
        Order order = orderSagaHelper.findOrder(data.getOrderId());
        orderDomainService.cancelOrder(order, data.getFailureMessages());
        orderSagaHelper.saveOrder(order);
        log.info("Order with id: {} is cancelled", order.getId().getValue());

        return EmptyEvent.getInstance();
    }
}
