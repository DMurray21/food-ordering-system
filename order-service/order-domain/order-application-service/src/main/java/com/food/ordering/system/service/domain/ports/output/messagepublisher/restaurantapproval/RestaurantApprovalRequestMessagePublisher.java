package com.food.ordering.system.service.domain.ports.output.messagepublisher.restaurantapproval;

import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.service.domain.outbox.model.approval.OrderApprovalOutboxMessage;
import com.food.ordering.system.service.domain.outbox.model.payment.OrderPaymentOutboxMessage;

import java.util.function.BiConsumer;

public interface RestaurantApprovalRequestMessagePublisher {

    void publish(OrderApprovalOutboxMessage orderApprovalOutboxMessage, BiConsumer<OrderApprovalOutboxMessage, OutboxStatus> outboxCallback);
}
