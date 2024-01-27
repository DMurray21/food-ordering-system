package com.food.ordering.system.service.domain.outbox.scheduler.approval;

import com.food.ordering.system.outbox.OutboxScheduler;
import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.saga.SagaStatus;
import com.food.ordering.system.service.domain.outbox.model.approval.OrderApprovalOutboxMessage;
import com.food.ordering.system.service.domain.outbox.model.payment.OrderPaymentOutboxMessage;
import com.food.ordering.system.service.domain.ports.output.messagepublisher.restaurantapproval.RestaurantApprovalRequestMessagePublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
public class RestaurantApprovalOutboxScheduler implements OutboxScheduler {

    private final ApprovalOutboxHelper approvalOutboxHelper;
    private final RestaurantApprovalRequestMessagePublisher restaurantApprovalRequestMessagePublisher;

    public RestaurantApprovalOutboxScheduler(ApprovalOutboxHelper approvalOutboxHelper, RestaurantApprovalRequestMessagePublisher restaurantApprovalRequestMessagePublisher) {
        this.approvalOutboxHelper = approvalOutboxHelper;
        this.restaurantApprovalRequestMessagePublisher = restaurantApprovalRequestMessagePublisher;
    }

    @Override
    @Transactional
    @Scheduled(fixedDelayString = "${order-service.outbox-scheduler-fixed-rate}", initialDelayString = "${order-service.outbox-scheduler-initial-delay}")
    public void processOutboxMessage() {
        Optional<List<OrderApprovalOutboxMessage>> messages = approvalOutboxHelper.getApprovalOutboxMessageByOutboxStatusAndSagaStatus(
                OutboxStatus.STARTED,
                SagaStatus.PROCESSING
        );

        if(messages.isPresent() && !messages.get().isEmpty()) {
            log.info("Received {} order approved messages with ids {}, sending to message bus",
                    messages.get().size(),
                    messages.get().stream().map(m -> m.getId().toString()).collect(Collectors.joining(","))
            );

            messages.get().forEach(m -> {
                restaurantApprovalRequestMessagePublisher.publish(m, this::updateOutboxStatus);
            });
        }

    }

    private void updateOutboxStatus(OrderApprovalOutboxMessage orderApprovalOutboxMessage, OutboxStatus outboxStatus) {
        orderApprovalOutboxMessage.setOutboxStatus(outboxStatus);
        approvalOutboxHelper.save(orderApprovalOutboxMessage);
        log.info("order approval message saved with status {}", outboxStatus.name());
    }
}
