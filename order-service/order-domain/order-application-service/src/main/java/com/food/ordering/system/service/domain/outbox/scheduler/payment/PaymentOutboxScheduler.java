package com.food.ordering.system.service.domain.outbox.scheduler.payment;

import com.food.ordering.system.outbox.OutboxScheduler;
import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.saga.SagaStatus;
import com.food.ordering.system.service.domain.outbox.model.payment.OrderPaymentOutboxMessage;
import com.food.ordering.system.service.domain.ports.output.messagepublisher.payment.PaymentRequestMessagePublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
public class PaymentOutboxScheduler implements OutboxScheduler {

    private final PaymentOutboxHelper paymentOutboxHelper;
    private final PaymentRequestMessagePublisher paymentRequestMessagePublisher;

    public PaymentOutboxScheduler(PaymentOutboxHelper paymentOutboxHelper, PaymentRequestMessagePublisher paymentRequestMessagePublisher) {
        this.paymentOutboxHelper = paymentOutboxHelper;
        this.paymentRequestMessagePublisher = paymentRequestMessagePublisher;
    }

    @Override
    @Transactional
    @Scheduled(fixedDelayString = "${order-service.outbox-scheduler-fixed-rate}", initialDelayString = "${order-service.outbox-scheduler-initial-delay}")
    public void processOutboxMessage() {

        Optional<List<OrderPaymentOutboxMessage>> outboxMessages = paymentOutboxHelper.getPaymentOutboxMessageByOutboxStatusAndSagaStatus(OutboxStatus.STARTED, SagaStatus.STARTED, SagaStatus.COMPENSATING);

        if(outboxMessages.isPresent() && !outboxMessages.get().isEmpty()) {
            List<OrderPaymentOutboxMessage> messages = outboxMessages.get();
            log.info("Received {} order payment outbox messages with ids {}, sending to message bus",
                    messages.size(),
                    messages.stream().map(m -> m.getId().toString()).collect(Collectors.joining(","))
            );

            messages.forEach(m -> {
                paymentRequestMessagePublisher.publish(m, this::updateOutboxStatus);
            });

            log.info("sent {} order payment messages to event bus", messages.size());
        }

    }

    private void updateOutboxStatus(OrderPaymentOutboxMessage message, OutboxStatus outboxStatus) {
        message.setOutboxStatus(outboxStatus);
        paymentOutboxHelper.save(message);
        log.info("Updated payment message");
    }
}
