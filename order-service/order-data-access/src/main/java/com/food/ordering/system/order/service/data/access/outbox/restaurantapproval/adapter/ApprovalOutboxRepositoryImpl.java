package com.food.ordering.system.order.service.data.access.outbox.restaurantapproval.adapter;

import com.food.ordering.system.order.service.data.access.outbox.payment.repository.PaymentOutboxJpaRepository;
import com.food.ordering.system.order.service.data.access.outbox.restaurantapproval.exception.ApprovalOutboxNotFoundException;
import com.food.ordering.system.order.service.data.access.outbox.restaurantapproval.mapper.ApprovalOutboxDataAccessMapper;
import com.food.ordering.system.order.service.data.access.outbox.restaurantapproval.repository.ApprovalOutboxJpaRepository;
import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.saga.SagaStatus;
import com.food.ordering.system.service.domain.outbox.model.approval.OrderApprovalOutboxMessage;
import com.food.ordering.system.service.domain.ports.output.repository.ApprovalOutboxRepository;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class ApprovalOutboxRepositoryImpl implements ApprovalOutboxRepository {

    private final ApprovalOutboxJpaRepository approvalOutboxJpaRepository;
    private final ApprovalOutboxDataAccessMapper approvalOutboxDataAccessMapper;

    public ApprovalOutboxRepositoryImpl(ApprovalOutboxJpaRepository approvalOutboxJpaRepository, ApprovalOutboxDataAccessMapper approvalOutboxDataAccessMapper) {
        this.approvalOutboxJpaRepository = approvalOutboxJpaRepository;
        this.approvalOutboxDataAccessMapper = approvalOutboxDataAccessMapper;
    }

    @Override
    public OrderApprovalOutboxMessage save(OrderApprovalOutboxMessage outboxMessage) {
        return approvalOutboxDataAccessMapper.approvalOutboxEntityToOrderApprovalOutboxMessage(
                approvalOutboxJpaRepository.save(approvalOutboxDataAccessMapper.orderCreatedOutboxMessageToOutboxEntity(outboxMessage))
        );
    }

    @Override
    public Optional<List<OrderApprovalOutboxMessage>> findByTypeAndOutboxStatusAndSagaStatus(String type, OutboxStatus outboxStatus, SagaStatus... sagaStatuses) {
        return Optional.of(
                approvalOutboxJpaRepository.findByTypeAndOutboxStatusAndSagaStatusIn(type, outboxStatus, Arrays.asList(sagaStatuses))
                        .orElseThrow(() -> new ApprovalOutboxNotFoundException("Could not find approval outbox entity"))
                        .stream()
                        .map(approvalOutboxDataAccessMapper::approvalOutboxEntityToOrderApprovalOutboxMessage)
                        .toList()
        );
    }

    @Override
    public Optional<OrderApprovalOutboxMessage> findByTypeAndSagaIdAndSagaStatus(String type, UUID sagaId, SagaStatus... sagaStatuses) {
        return approvalOutboxJpaRepository.findByTypeAndSagaIdAndSagaStatusIn(type, sagaId, Arrays.asList(sagaStatuses)).map(approvalOutboxDataAccessMapper::approvalOutboxEntityToOrderApprovalOutboxMessage);
    }

    @Override
    public void deleteByTypeAndOutboxStatusAndSagaStatus(String type, OutboxStatus outboxStatus, SagaStatus... sagaStatuses) {
        approvalOutboxJpaRepository.deleteByTypeAndOutboxStatusAndSagaStatusIn(type, outboxStatus, Arrays.asList(sagaStatuses));
    }
}
