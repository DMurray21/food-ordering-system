package com.food.ordering.system.service.domain;

import com.food.ordering.system.domain.event.EmptyEvent;
import com.food.ordering.system.domain.valueobject.OrderStatus;
import com.food.ordering.system.domain.valueobject.PaymentStatus;
import com.food.ordering.system.order.service.domain.OrderDomainService;
import com.food.ordering.system.order.service.domain.entity.Order;
import com.food.ordering.system.order.service.domain.event.OrderPaidEvent;
import com.food.ordering.system.order.service.domain.exception.OrderDomainException;
import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.saga.SagaStatus;
import com.food.ordering.system.saga.SagaStep;
import com.food.ordering.system.service.domain.dto.message.PaymentResponse;
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

@Slf4j
@Component
public class OrderPaymentSaga implements SagaStep<PaymentResponse> {

    private final OrderDomainService orderDomainService;
    private final OrderSagaHelper orderSagaHelper;

    private final PaymentOutboxHelper paymentOutboxHelper;

    private final ApprovalOutboxHelper approvalOutboxHelper;

    private final OrderDataMapper orderDataMapper;


    public OrderPaymentSaga(OrderDomainService orderDomainService, OrderSagaHelper orderSagaHelper, PaymentOutboxHelper paymentOutboxHelper, ApprovalOutboxHelper approvalOutboxHelper, OrderDataMapper orderDataMapper) {
        this.orderDomainService = orderDomainService;
        this.orderSagaHelper = orderSagaHelper;
        this.paymentOutboxHelper = paymentOutboxHelper;
        this.approvalOutboxHelper = approvalOutboxHelper;
        this.orderDataMapper = orderDataMapper;
    }

    @Override
    @Transactional
    public void process(PaymentResponse data) {

        Optional<OrderPaymentOutboxMessage> orderPaymentOutboxMessage = paymentOutboxHelper.getPaymentOutboxMessageBySagaIdAndSagaStatus(UUID.fromString(data.getSagaId()), SagaStatus.STARTED);

        if(orderPaymentOutboxMessage.isEmpty()) {
            log.info("An outbox message with with saga id {} has already been processed", data.getSagaId());
            return;
        }

        OrderPaymentOutboxMessage message = orderPaymentOutboxMessage.get();

        OrderPaidEvent orderPaidEvent = completeOrder(data);

        SagaStatus sagaStatus = orderSagaHelper.orderStatusToSagaStatus(orderPaidEvent.getOrder().getOrderStatus());
        paymentOutboxHelper.save(getUpdatedPaymentOutboxMessage(message, orderPaidEvent.getOrder().getOrderStatus(), sagaStatus));

        approvalOutboxHelper.saveApprovalOutboxMessage(
                orderDataMapper.orderPaidEventToOrderApprovalEventPayload(orderPaidEvent),
                orderPaidEvent.getOrder().getOrderStatus(),
                sagaStatus,
                OutboxStatus.STARTED,
                UUID.fromString(data.getSagaId())
        );

        log.info("Order with id: {} is paid", data.getOrderId());
    }

    private OrderPaymentOutboxMessage getUpdatedPaymentOutboxMessage(OrderPaymentOutboxMessage message, OrderStatus orderStatus, SagaStatus sagaStatus) {
        message.setProcessedAt(ZonedDateTime.now(ZoneOffset.UTC));
        message.setOrderStatus(orderStatus);
        message.setSagaStatus(sagaStatus);
        return message;
    }

    private OrderPaidEvent completeOrder(PaymentResponse paymentResponse) {
        log.info("Completing payment for order with id: {}", paymentResponse.getOrderId());
        Order order = orderSagaHelper.findOrder(paymentResponse.getOrderId());
        OrderPaidEvent orderPaidEvent = orderDomainService.payOrder(order);
        orderSagaHelper.saveOrder(order);

        return orderPaidEvent;
    }

    @Override
    @Transactional
    public void rollback(PaymentResponse data) {

        Optional<OrderPaymentOutboxMessage> orderPaymentOutboxMessageOptional = paymentOutboxHelper.getPaymentOutboxMessageBySagaIdAndSagaStatus(
                UUID.fromString(data.getSagaId()),
                getCurrentSagaStatus(data.getPaymentStatus())
        );

        if(orderPaymentOutboxMessageOptional.isEmpty()) {
            log.info("An outbox message with id {} has already been processed", data.getSagaId());
            return;
        }

        OrderPaymentOutboxMessage orderPaymentOutboxMessage = orderPaymentOutboxMessageOptional.get();

        Order order = rollbackOrder(data);
        SagaStatus sagaStatus = orderSagaHelper.orderStatusToSagaStatus(order.getOrderStatus());

        paymentOutboxHelper.save(getUpdatedPaymentOutboxMessage(orderPaymentOutboxMessage, order.getOrderStatus(), sagaStatus));

        if(data.getPaymentStatus() == PaymentStatus.CANCELLED) {
            approvalOutboxHelper.save(getUpdatedApprovalOutboxMessage(data.getSagaId(), order.getOrderStatus(), sagaStatus));
        }

        log.info("Order with id: {} is cancelled", order.getId().getValue());
    }

    private OrderApprovalOutboxMessage getUpdatedApprovalOutboxMessage(String sagaId, OrderStatus orderStatus, SagaStatus sagaStatus) {
        Optional<OrderApprovalOutboxMessage> orderApprovalOutboxMessage = approvalOutboxHelper.getApprovalOutboxMessageBySagaIdAndSagaStatus(
                UUID.fromString(sagaId),
                SagaStatus.COMPENSATING
        );

        if(orderApprovalOutboxMessage.isEmpty()) {
            throw new OrderDomainException("Approval outbox message could not be found in " + SagaStatus.COMPENSATING + " status");
        }

        OrderApprovalOutboxMessage outboxMessage = orderApprovalOutboxMessage.get();
        outboxMessage.setProcessedAt(ZonedDateTime.now(ZoneOffset.UTC));
        outboxMessage.setOrderStatus(orderStatus);
        outboxMessage.setSagaStatus(sagaStatus);

        return outboxMessage;
    }

    private SagaStatus[] getCurrentSagaStatus(PaymentStatus paymentStatus) {
        return switch (paymentStatus) {
            case COMPLETED -> new SagaStatus[] {SagaStatus.STARTED};
            case CANCELLED -> new SagaStatus[] {SagaStatus.PROCESSING};
            case FAILED -> new SagaStatus[] {SagaStatus.STARTED, SagaStatus.PROCESSING};
        };
    }

    private Order rollbackOrder(PaymentResponse data) {
        log.info("Cancelling order with id {}", data.getOrderId());
        Order order = orderSagaHelper.findOrder(data.getOrderId());
        orderDomainService.cancelOrder(order, data.getFailureMessages());
        orderSagaHelper.saveOrder(order);

        return order;
    }
}
