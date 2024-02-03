package com.food.ordering.system.order.service.data.access.outbox.restaurantapproval.mapper;

import com.food.ordering.system.order.service.data.access.outbox.payment.entity.PaymentOutboxEntity;
import com.food.ordering.system.order.service.data.access.outbox.restaurantapproval.entity.ApprovalOutboxEntity;
import com.food.ordering.system.service.domain.outbox.model.approval.OrderApprovalOutboxMessage;
import com.food.ordering.system.service.domain.outbox.model.payment.OrderPaymentOutboxMessage;

public class ApprovalOutboxDataAccessMapper {

    public ApprovalOutboxEntity orderCreatedOutboxMessageToOutboxEntity(OrderApprovalOutboxMessage orderApprovalOutboxMessage) {
        return ApprovalOutboxEntity.builder()
                .id(orderApprovalOutboxMessage.getId())
                .sagaId(orderApprovalOutboxMessage.getSagaId())
                .orderStatus(orderApprovalOutboxMessage.getOrderStatus())
                .sagaStatus(orderApprovalOutboxMessage.getSagaStatus())
                .outboxStatus(orderApprovalOutboxMessage.getOutboxStatus())
                .version(orderApprovalOutboxMessage.getVersion())
                .type(orderApprovalOutboxMessage.getType())
                .payload(orderApprovalOutboxMessage.getPayload())
                .processedAt(orderApprovalOutboxMessage.getProcessedAt())
                .createdAt(orderApprovalOutboxMessage.getCreatedAt())
                .build();
    }

    public OrderApprovalOutboxMessage approvalOutboxEntityToOrderApprovalOutboxMessage(ApprovalOutboxEntity approvalOutboxEntity) {
        return OrderApprovalOutboxMessage.builder()
                .id(approvalOutboxEntity.getId())
                .sagaId(approvalOutboxEntity.getSagaId())
                .createdAt(approvalOutboxEntity.getCreatedAt())
                .processedAt(approvalOutboxEntity.getProcessedAt())
                .type(approvalOutboxEntity.getType())
                .payload(approvalOutboxEntity.getPayload())
                .orderStatus(approvalOutboxEntity.getOrderStatus())
                .outboxStatus(approvalOutboxEntity.getOutboxStatus())
                .sagaStatus(approvalOutboxEntity.getSagaStatus())
                .version(approvalOutboxEntity.getVersion())
                .build();
    }
}
