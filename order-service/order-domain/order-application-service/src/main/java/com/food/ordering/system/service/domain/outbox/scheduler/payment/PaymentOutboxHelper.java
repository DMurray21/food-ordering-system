package com.food.ordering.system.service.domain.outbox.scheduler.payment;

import com.food.ordering.system.order.service.domain.exception.OrderDomainException;
import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.saga.SagaStatus;
import com.food.ordering.system.saga.order.SagaConstants;
import com.food.ordering.system.service.domain.outbox.model.payment.OrderPaymentOutboxMessage;
import com.food.ordering.system.service.domain.ports.output.repository.PaymentOutboxRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@Slf4j
public class PaymentOutboxHelper {

    private final PaymentOutboxRepository paymentOutboxRepository;

    public PaymentOutboxHelper(PaymentOutboxRepository paymentOutboxRepository) {
        this.paymentOutboxRepository = paymentOutboxRepository;
    }

    @Transactional(readOnly = true)
    public Optional<List<OrderPaymentOutboxMessage>> getPaymentOutboxMessageByOutboxStatusAndSagaStatus(OutboxStatus outboxStatus, SagaStatus... sagaStatuses) {
        return paymentOutboxRepository.findByTypeAndOutboxStatusAndSagaStatus(SagaConstants.ORDER_SAGA_NAME, outboxStatus, sagaStatuses);
    }

    @Transactional(readOnly = true)
    public Optional<OrderPaymentOutboxMessage> getPaymentOutboxMessageBySagaIdAndSagaStatus(UUID sagaId, SagaStatus... sagaStatuses) {
        return paymentOutboxRepository.findByTypeAndSagaIdAndSagaStatus(SagaConstants.ORDER_SAGA_NAME, sagaId, sagaStatuses);
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

    @Transactional
    public void deletePaymentOutboxMessageByOutboxStatusAndSagaStatus(OutboxStatus outboxStatus, SagaStatus... sagaStatuses) {
        paymentOutboxRepository.deleteByTypeAndOutboxStatusAndSagaStatus(SagaConstants.ORDER_SAGA_NAME, outboxStatus, sagaStatuses);
    }
}
