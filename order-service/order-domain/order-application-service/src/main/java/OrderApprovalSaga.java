package com.food.ordering.system.service.domain;

import com.food.ordering.system.domain.event.EmptyEvent;
import com.food.ordering.system.order.service.domain.OrderDomainService;
import com.food.ordering.system.order.service.domain.entity.Order;
import com.food.ordering.system.order.service.domain.event.OrderCancelledEvent;
import com.food.ordering.system.saga.SagaStep;
import com.food.ordering.system.service.domain.dto.message.RestaurantApprovalResponse;
import com.food.ordering.system.service.domain.ports.output.messagepublisher.payment.OrderCancelledPaymentRequestMessagePublisher;
import com.food.ordering.system.service.domain.ports.output.repository.OrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
public class OrderApprovalSaga implements SagaStep<RestaurantApprovalResponse, EmptyEvent, OrderCancelledEvent> {

    private final OrderDomainService orderDomainService;
    private final OrderSagaHelper orderSagaHelper;
    private final OrderCancelledPaymentRequestMessagePublisher orderCancelledPaymentRequestMessagePublisher;

    public OrderApprovalSaga(OrderDomainService orderDomainService, OrderSagaHelper orderSagaHelper, OrderCancelledPaymentRequestMessagePublisher orderCancelledPaymentRequestMessagePublisher) {
        this.orderDomainService = orderDomainService;
        this.orderSagaHelper = orderSagaHelper;
        this.orderCancelledPaymentRequestMessagePublisher = orderCancelledPaymentRequestMessagePublisher;
    }

    @Override
    @Transactional
    public EmptyEvent process(RestaurantApprovalResponse data) {
        log.info("Processing approved order {}", data.getOrderId());
        Order order = orderSagaHelper.findOrder(data.getOrderId());
        orderDomainService.approveOrder(order);
        log.info("Order {} approved", data.getOrderId());
        return EmptyEvent.getInstance();
    }

    @Override
    @Transactional
    public OrderCancelledEvent rollback(RestaurantApprovalResponse data) {
        log.info("rolling back order {}", data.getOrderId());
        Order order = orderSagaHelper.findOrder(data.getOrderId());
        OrderCancelledEvent orderCancelledEvent = orderDomainService.cancelOrderPayment(order, data.getFailureMessages(), orderCancelledPaymentRequestMessagePublisher);
        orderSagaHelper.saveOrder(order);
        log.info("order cancelled");
        return orderCancelledEvent;
    }
}
