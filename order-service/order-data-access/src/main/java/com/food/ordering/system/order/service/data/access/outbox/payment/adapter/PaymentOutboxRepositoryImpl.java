package com.food.ordering.system.order.service.data.access.outbox.payment.adapter;

import com.food.ordering.system.order.service.data.access.outbox.payment.exception.PaymentOutboxNotFoundException;
import com.food.ordering.system.order.service.data.access.outbox.payment.mapper.PaymentOutboxDataAccessMapper;
import com.food.ordering.system.order.service.data.access.outbox.payment.repository.PaymentOutboxJpaRepository;
import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.saga.SagaStatus;
import com.food.ordering.system.service.domain.outbox.model.payment.OrderPaymentOutboxMessage;
import com.food.ordering.system.service.domain.ports.output.repository.PaymentOutboxRepository;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class PaymentOutboxRepositoryImpl implements PaymentOutboxRepository {

    private final PaymentOutboxJpaRepository paymentOutboxJpaRepository;
    private final PaymentOutboxDataAccessMapper paymentOutboxDataAccessMapper;

    public PaymentOutboxRepositoryImpl(PaymentOutboxJpaRepository paymentOutboxJpaRepository, PaymentOutboxDataAccessMapper paymentOutboxDataAccessMapper) {
        this.paymentOutboxJpaRepository = paymentOutboxJpaRepository;
        this.paymentOutboxDataAccessMapper = paymentOutboxDataAccessMapper;
    }

    @Override
    public OrderPaymentOutboxMessage save(OrderPaymentOutboxMessage outboxMessage) {
        return paymentOutboxDataAccessMapper.paymentOutBoxEntityToOrderPaymentOutboxMessage(
                paymentOutboxJpaRepository.save(
                        paymentOutboxDataAccessMapper.orderPaymentOutboxMessageToPaymentOutboxEntity(outboxMessage)
                )
        );
    }

    @Override
    public Optional<List<OrderPaymentOutboxMessage>> findByTypeAndOutboxStatusAndSagaStatus(String type, OutboxStatus outboxStatus, SagaStatus... sagaStatuses) {
        return Optional.of(paymentOutboxJpaRepository.findByTypeAndOutboxStatusAndSagaStatusIn(type, outboxStatus, Arrays.asList(sagaStatuses))
                .orElseThrow(() -> new PaymentOutboxNotFoundException("Payment outbox object could not be found for saga type " + type))
                .stream()
                .map(paymentOutboxDataAccessMapper::paymentOutBoxEntityToOrderPaymentOutboxMessage)
                .toList());
    }

    @Override
    public Optional<OrderPaymentOutboxMessage> findByTypeAndSagaIdAndSagaStatus(String type, UUID sagaId, SagaStatus... sagaStatuses) {
        return paymentOutboxJpaRepository.findByTypeAndSagaIdAndSagaStatusIn(type, sagaId, Arrays.asList(sagaStatuses))
                .map(paymentOutboxDataAccessMapper::paymentOutBoxEntityToOrderPaymentOutboxMessage);
    }

    @Override
    public void deleteByTypeAndOutboxStatusAndSagaStatus(String type, OutboxStatus outboxStatus, SagaStatus... sagaStatuses) {
        paymentOutboxJpaRepository.deleteByTypeAndOutboxStatusAndSagaStatus(type, outboxStatus, Arrays.asList(sagaStatuses));
    }
}
