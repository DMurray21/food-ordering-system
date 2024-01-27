package com.food.ordering.system.service.domain.ports.output.repository;

import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.saga.SagaStatus;
import com.food.ordering.system.service.domain.outbox.model.approval.OrderApprovalOutboxMessage;
import com.food.ordering.system.service.domain.outbox.model.payment.OrderPaymentOutboxMessage;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ApprovalOutboxRepository {

    OrderApprovalOutboxMessage save(OrderApprovalOutboxMessage outboxMessage);

    Optional<List<OrderApprovalOutboxMessage>> findByTypeAndOutboxStatusAndSagaStatus(String type, OutboxStatus outboxStatus, SagaStatus... sagaStatuses);

    Optional<OrderApprovalOutboxMessage> findByTypeAndSagaIdAndSagaStatus(String type, UUID sagaId, SagaStatus... sagaStatuses);

    void deleteByTypeAndOutboxStatusAndSagaStatus(String type, OutboxStatus outboxStatus, SagaStatus... sagaStatuses);
}
