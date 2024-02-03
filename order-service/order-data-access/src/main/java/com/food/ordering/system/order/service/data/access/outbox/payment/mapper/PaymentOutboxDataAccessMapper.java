package com.food.ordering.system.order.service.data.access.outbox.payment.mapper;

import com.food.ordering.system.order.service.data.access.outbox.payment.entity.PaymentOutboxEntity;
import com.food.ordering.system.service.domain.outbox.model.payment.OrderPaymentOutboxMessage;
import org.springframework.stereotype.Component;

@Component
public class PaymentOutboxDataAccessMapper {

    public PaymentOutboxEntity orderPaymentOutboxMessageToPaymentOutboxEntity(OrderPaymentOutboxMessage orderPaymentOutboxMessage) {
        return PaymentOutboxEntity.builder()
                .id(orderPaymentOutboxMessage.getId())
                .sagaId(orderPaymentOutboxMessage.getSagaId())
                .orderStatus(orderPaymentOutboxMessage.getOrderStatus())
                .sagaStatus(orderPaymentOutboxMessage.getSagaStatus())
                .outboxStatus(orderPaymentOutboxMessage.getOutboxStatus())
                .version(orderPaymentOutboxMessage.getVersion())
                .type(orderPaymentOutboxMessage.getType())
                .payload(orderPaymentOutboxMessage.getPayload())
                .processedAt(orderPaymentOutboxMessage.getProcessedAt())
                .createdAt(orderPaymentOutboxMessage.getCreatedAt())
                .build();
    }

    public OrderPaymentOutboxMessage paymentOutBoxEntityToOrderPaymentOutboxMessage(PaymentOutboxEntity paymentOutboxEntity) {
        return OrderPaymentOutboxMessage.builder()
                .id(paymentOutboxEntity.getId())
                .sagaId(paymentOutboxEntity.getSagaId())
                .createdAt(paymentOutboxEntity.getCreatedAt())
                .processedAt(paymentOutboxEntity.getProcessedAt())
                .type(paymentOutboxEntity.getType())
                .payload(paymentOutboxEntity.getPayload())
                .orderStatus(paymentOutboxEntity.getOrderStatus())
                .outboxStatus(paymentOutboxEntity.getOutboxStatus())
                .sagaStatus(paymentOutboxEntity.getSagaStatus())
                .version(paymentOutboxEntity.getVersion())
                .build();
    }

}
