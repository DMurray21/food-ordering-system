package com.food.ordering.system.service.domain.outbox.scheduler.payment;

import com.food.ordering.system.outbox.OutboxScheduler;
import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.saga.SagaStatus;
import com.food.ordering.system.service.domain.OrderPaymentSaga;
import com.food.ordering.system.service.domain.outbox.model.payment.OrderPaymentOutboxMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
public class PaymentOutboxCleanerScheduler implements OutboxScheduler {

    private final PaymentOutboxHelper paymentOutboxHelper;

    public PaymentOutboxCleanerScheduler(PaymentOutboxHelper paymentOutboxHelper) {
        this.paymentOutboxHelper = paymentOutboxHelper;
    }

    @Override
    @Scheduled(cron = "@midnight")
    public void processOutboxMessage() {

        Optional<List<OrderPaymentOutboxMessage>> messages = paymentOutboxHelper.getPaymentOutboxMessageByOutboxStatusAndSagaStatus(OutboxStatus.COMPLETED,
                SagaStatus.SUCCEEDED,
                SagaStatus.FAILED,
                SagaStatus.COMPENSATED
        );

        if(messages.isPresent()) {
            List<OrderPaymentOutboxMessage> outboxMessages = messages.get();
            paymentOutboxHelper.deletePaymentOutboxMessageByOutboxStatusAndSagaStatus(OutboxStatus.COMPLETED,
                    SagaStatus.SUCCEEDED,
                    SagaStatus.FAILED,
                    SagaStatus.COMPENSATED
            );
        }

    }
}
