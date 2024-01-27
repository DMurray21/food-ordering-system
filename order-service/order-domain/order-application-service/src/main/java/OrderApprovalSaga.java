package com.food.ordering.system.service.domain;

import com.food.ordering.system.domain.event.EmptyEvent;
import com.food.ordering.system.domain.valueobject.OrderStatus;
import com.food.ordering.system.order.service.domain.OrderDomainService;
import com.food.ordering.system.order.service.domain.entity.Order;
import com.food.ordering.system.order.service.domain.event.OrderCancelledEvent;
import com.food.ordering.system.order.service.domain.exception.OrderDomainException;
import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.saga.SagaStatus;
import com.food.ordering.system.saga.SagaStep;
import com.food.ordering.system.service.domain.dto.message.RestaurantApprovalResponse;
import com.food.ordering.system.service.domain.mapper.OrderDataMapper;
import com.food.ordering.system.service.domain.outbox.model.approval.OrderApprovalOutboxMessage;
import com.food.ordering.system.service.domain.outbox.model.payment.OrderPaymentOutboxMessage;
import com.food.ordering.system.service.domain.outbox.scheduler.approval.ApprovalOutboxHelper;
import com.food.ordering.system.service.domain.outbox.scheduler.payment.PaymentOutboxHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

@Component
@Slf4j
public class OrderApprovalSaga implements SagaStep<RestaurantApprovalResponse> {

    private final OrderDomainService orderDomainService;
    private final OrderSagaHelper orderSagaHelper;
    private final PaymentOutboxHelper paymentOutboxHelper;
    private final ApprovalOutboxHelper approvalOutboxHelper;
    private final OrderDataMapper orderDataMapper;

    public OrderApprovalSaga(OrderDomainService orderDomainService, OrderSagaHelper orderSagaHelper, PaymentOutboxHelper paymentOutboxHelper, ApprovalOutboxHelper approvalOutboxHelper, OrderDataMapper orderDataMapper) {
        this.orderDomainService = orderDomainService;
        this.orderSagaHelper = orderSagaHelper;
        this.paymentOutboxHelper = paymentOutboxHelper;
        this.approvalOutboxHelper = approvalOutboxHelper;
        this.orderDataMapper = orderDataMapper;
    }

    @Override
    @Transactional
    public void process(RestaurantApprovalResponse data) {

        Optional<OrderApprovalOutboxMessage> orderApprovalOutboxMessageOptional = approvalOutboxHelper.getApprovalOutboxMessageBySagaIdAndSagaStatus(
                UUID.fromString(data.getSagaId()),
                SagaStatus.PROCESSING
        );

        if(orderApprovalOutboxMessageOptional.isEmpty()) {
            log.info("Could not find a processing saga for id {}", data.getSagaId());
            return;
        }

        OrderApprovalOutboxMessage orderApprovalOutboxMessage = orderApprovalOutboxMessageOptional.get();

        Order order = approveOrder(data);

        SagaStatus sagaStatus = orderSagaHelper.orderStatusToSagaStatus(order.getOrderStatus());

        approvalOutboxHelper.save(getUpdatedApprovalOutboxMessage(orderApprovalOutboxMessage, order.getOrderStatus(), sagaStatus));

        paymentOutboxHelper.save(getUpdatedPaymentOutboxMessage(data.getSagaId(), order.getOrderStatus(), sagaStatus));

        log.info("Order {} approved", data.getOrderId());

    }

    private OrderApprovalOutboxMessage getUpdatedApprovalOutboxMessage(OrderApprovalOutboxMessage orderApprovalOutboxMessage, OrderStatus orderStatus, SagaStatus sagaStatus) {
        orderApprovalOutboxMessage.setOrderStatus(orderStatus);
        orderApprovalOutboxMessage.setSagaStatus(sagaStatus);
        orderApprovalOutboxMessage.setProcessedAt(ZonedDateTime.now(ZoneOffset.UTC));
        return orderApprovalOutboxMessage;

    }

    private OrderPaymentOutboxMessage getUpdatedPaymentOutboxMessage(String sagaId, OrderStatus orderStatus, SagaStatus sagaStatus) {
        Optional<OrderPaymentOutboxMessage> orderPaymentOutboxMessage = paymentOutboxHelper.getPaymentOutboxMessageBySagaIdAndSagaStatus(
                UUID.fromString(sagaId),
                sagaStatus
        );

        if(orderPaymentOutboxMessage.isEmpty()) {
            throw new OrderDomainException("Order payment message with id " +  sagaId +" not found");
        }

        OrderPaymentOutboxMessage outboxMessage = orderPaymentOutboxMessage.get();
        outboxMessage.setSagaStatus(sagaStatus);
        outboxMessage.setOrderStatus(orderStatus);
        outboxMessage.setProcessedAt(ZonedDateTime.now(ZoneOffset.UTC));

        return outboxMessage;
    }

    private Order approveOrder(RestaurantApprovalResponse data) {
        log.info("Processing approved order {}", data.getOrderId());
        Order order = orderSagaHelper.findOrder(data.getOrderId());
        orderDomainService.approveOrder(order);

        return order;
    }

    @Override
    @Transactional
    public void rollback(RestaurantApprovalResponse data) {

        Optional<OrderApprovalOutboxMessage> orderApprovalOutboxMessageOptional = approvalOutboxHelper.getApprovalOutboxMessageBySagaIdAndSagaStatus(
                UUID.fromString(data.getSagaId()),
                SagaStatus.PROCESSING
        );

        if(orderApprovalOutboxMessageOptional.isEmpty()) {
            return;
        }

        OrderApprovalOutboxMessage orderApprovalOutboxMessage = orderApprovalOutboxMessageOptional.get();
        OrderCancelledEvent orderCancelledEvent = rollbackOrder(data);

        SagaStatus sagaStatus = orderSagaHelper.orderStatusToSagaStatus(orderCancelledEvent.getOrder().getOrderStatus());

        approvalOutboxHelper.save(getUpdatedApprovalOutboxMessage(orderApprovalOutboxMessage, orderCancelledEvent.getOrder().getOrderStatus(), sagaStatus));

        paymentOutboxHelper.savePaymentOutboxMessage(
                orderDataMapper.orderCancelledEventToOrderPaymentEventPayload(orderCancelledEvent),
                orderCancelledEvent.getOrder().getOrderStatus(),
                sagaStatus,
                OutboxStatus.STARTED,
                UUID.fromString(data.getSagaId())
        );

        log.info("order cancelled");
    }

    private OrderCancelledEvent rollbackOrder(RestaurantApprovalResponse data) {
        log.info("rolling back order {}", data.getOrderId());
        Order order = orderSagaHelper.findOrder(data.getOrderId());
        OrderCancelledEvent orderCancelledEvent = orderDomainService.cancelOrderPayment(order, data.getFailureMessages());
        orderSagaHelper.saveOrder(order);

        return orderCancelledEvent;
    }
}
