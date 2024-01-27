package com.food.ordering.system.service.domain.outbox.scheduler.payment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.food.ordering.system.domain.valueobject.OrderStatus;
import com.food.ordering.system.order.service.domain.exception.OrderDomainException;
import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.saga.SagaStatus;
import com.food.ordering.system.saga.order.SagaConstants;
import com.food.ordering.system.service.domain.outbox.model.payment.OrderPaymentEventPayload;
import com.food.ordering.system.service.domain.outbox.model.payment.OrderPaymentOutboxMessage;
import com.food.ordering.system.service.domain.ports.output.repository.PaymentOutboxRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.food.ordering.system.saga.order.SagaConstants.ORDER_SAGA_NAME;

@Component
@Slf4j
public class PaymentOutboxHelper {

    private final PaymentOutboxRepository paymentOutboxRepository;
    private final ObjectMapper objectMapper;

    public PaymentOutboxHelper(PaymentOutboxRepository paymentOutboxRepository, ObjectMapper objectMapper) {
        this.paymentOutboxRepository = paymentOutboxRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public Optional<List<OrderPaymentOutboxMessage>> getPaymentOutboxMessageByOutboxStatusAndSagaStatus(OutboxStatus outboxStatus, SagaStatus... sagaStatuses) {
        return paymentOutboxRepository.findByTypeAndOutboxStatusAndSagaStatus(ORDER_SAGA_NAME, outboxStatus, sagaStatuses);
    }

    @Transactional(readOnly = true)
    public Optional<OrderPaymentOutboxMessage> getPaymentOutboxMessageBySagaIdAndSagaStatus(UUID sagaId, SagaStatus... sagaStatuses) {
        return paymentOutboxRepository.findByTypeAndSagaIdAndSagaStatus(ORDER_SAGA_NAME, sagaId, sagaStatuses);
    }

    @Transactional
    public void save(OrderPaymentOutboxMessage orderPaymentOutboxMessage) {
        OrderPaymentOutboxMessage outboxMessage = paymentOutboxRepository.save(orderPaymentOutboxMessage);
        if(outboxMessage == null) {
            log.error("Could not save order payment message with id {}", orderPaymentOutboxMessage.getId());
            throw new OrderDomainException("Could not save order payment message with id " + orderPaymentOutboxMessage.getId());
        }

        log.info("order payment message saved with id {}", orderPaymentOutboxMessage.getId());
    }

    public void savePaymentOutboxMessage(OrderPaymentEventPayload paymentEventPayload, OrderStatus orderStatus, SagaStatus sagaStatus, OutboxStatus outboxStatus, UUID sagaId) {
        save(OrderPaymentOutboxMessage.builder()
                .outboxStatus(outboxStatus)
                .orderStatus(orderStatus)
                .sagaId(sagaId)
                .id(UUID.randomUUID())
                .type(ORDER_SAGA_NAME)
                .payload(createPayload(paymentEventPayload))
                .build());
    }

    private String createPayload(OrderPaymentEventPayload paymentEventPayload) {
        try {
            return objectMapper.writeValueAsString(paymentEventPayload);
        } catch (JsonProcessingException e) {
            log.error("Could not create OrderPaymentEventPayload object for order id {}", paymentEventPayload.getOrderId());
            throw new OrderDomainException("Could not create OrderPaymentEventPayload object for order id " + paymentEventPayload.getOrderId());
        }
    }

    @Transactional
    public void deletePaymentOutboxMessageByOutboxStatusAndSagaStatus(OutboxStatus outboxStatus, SagaStatus... sagaStatuses) {
        paymentOutboxRepository.deleteByTypeAndOutboxStatusAndSagaStatus(ORDER_SAGA_NAME, outboxStatus, sagaStatuses);
    }
}
