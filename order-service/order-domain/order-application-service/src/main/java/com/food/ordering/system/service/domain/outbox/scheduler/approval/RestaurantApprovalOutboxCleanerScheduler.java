package com.food.ordering.system.service.domain.outbox.scheduler.approval;

import com.food.ordering.system.outbox.OutboxScheduler;
import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.saga.SagaStatus;
import com.food.ordering.system.service.domain.outbox.model.approval.OrderApprovalOutboxMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
public class RestaurantApprovalOutboxCleanerScheduler implements OutboxScheduler {

    private final ApprovalOutboxHelper approvalOutboxHelper;

    public RestaurantApprovalOutboxCleanerScheduler(ApprovalOutboxHelper approvalOutboxHelper) {
        this.approvalOutboxHelper = approvalOutboxHelper;
    }

    @Override
    @Scheduled(cron = "@midnight")
    public void processOutboxMessage() {

        Optional<List<OrderApprovalOutboxMessage>> orderApprovalOutboxMessages = approvalOutboxHelper.getApprovalOutboxMessageByOutboxStatusAndSagaStatus(
                OutboxStatus.COMPLETED,
                SagaStatus.SUCCEEDED,
                SagaStatus.FAILED,
                SagaStatus.COMPENSATED
        );

        if(orderApprovalOutboxMessages.isPresent()) {
            List<OrderApprovalOutboxMessage> messages = orderApprovalOutboxMessages.get();

            approvalOutboxHelper.deleteApprovalOutboxMessageByOutboxStatus(
                    OutboxStatus.COMPLETED,
                    SagaStatus.SUCCEEDED,
                    SagaStatus.FAILED,
                    SagaStatus.COMPENSATED
            );

            log.info("{} outbox approval messages deleted", messages.size());
        }

    }
}
