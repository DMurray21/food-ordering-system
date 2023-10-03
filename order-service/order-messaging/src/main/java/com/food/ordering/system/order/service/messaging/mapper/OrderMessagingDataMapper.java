package com.food.ordering.system.order.service.messaging.mapper;

import com.food.ordering.system.domain.valueobject.OrderApprovalStatus;
import com.food.ordering.system.domain.valueobject.PaymentStatus;
import com.food.ordering.system.kafka.order.avro.model.*;
import com.food.ordering.system.order.service.domain.entity.Order;
import com.food.ordering.system.order.service.domain.event.OrderCancelledEvent;
import com.food.ordering.system.order.service.domain.event.OrderCreatedEvent;
import com.food.ordering.system.order.service.domain.event.OrderPaidEvent;
import com.food.ordering.system.service.domain.dto.message.PaymentResponse;
import com.food.ordering.system.service.domain.dto.message.RestaurantApprovalResponse;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class OrderMessagingDataMapper {

    public PaymentRequestAvroModel orderCreatedEventToPaymentRequestAvroModel(OrderCreatedEvent orderCreatedEvent) {
        Order order = orderCreatedEvent.getOrder();
        return PaymentRequestAvroModel.newBuilder()
                .setId(UUID.randomUUID().toString())
                .setSagaId("")
                .setOrderId(order.getId().getValue().toString())
                .setCustomerId(order.getCustomerId().getValue().toString())
                .setPrice(order.getPrice().getAmount())
                .setCreatedAt(orderCreatedEvent.getCreatedAt().toEpochSecond())
                .setPaymentOrderStatus(PaymentOrderStatus.PENDING)
                .build();
    }

    public PaymentRequestAvroModel orderCancelledEventToPaymentRequestAvroModel(OrderCancelledEvent orderCancelledEvent) {
        Order order = orderCancelledEvent.getOrder();
        return PaymentRequestAvroModel.newBuilder()
                .setId(UUID.randomUUID().toString())
                .setSagaId("")
                .setOrderId(order.getId().getValue().toString())
                .setCustomerId(order.getCustomerId().getValue().toString())
                .setPrice(order.getPrice().getAmount())
                .setCreatedAt(orderCancelledEvent.getCreatedAt().toEpochSecond())
                .setPaymentOrderStatus(PaymentOrderStatus.CANCELLED)
                .build();
    }

    public RestaurantApprovalRequestAvroModel orderPaidEventToRestaurantRequest(OrderPaidEvent orderPaidEvent) {
        Order order = orderPaidEvent.getOrder();
        return RestaurantApprovalRequestAvroModel.newBuilder()
                .setId(UUID.randomUUID().toString())
                .setSagaId("")
                .setOrderId(order.getId().getValue().toString())
                .setRestaurantId(order.getRestaurantId().getValue().toString())
                .setProducts(order.getOrderItems().stream()
                        .map(i -> Product.newBuilder()
                                .setId(i.getProduct().getId().getValue().toString())
                                .setQuantity(i.getQuantity())
                                .build())
                        .toList())
                .setPrice(order.getPrice().getAmount())
                .setCreatedAt(orderPaidEvent.getCreatedAt().toEpochSecond())
                .setRestaurantOrderStatus(RestaurantOrderStatus.PAID)
                .build();
    }

    public PaymentResponse paymentResponseAvroModelToPaymentResponse(PaymentResponseAvroModel paymentResponseAvroModel) {
     return PaymentResponse.builder()
             .id(paymentResponseAvroModel.getId())
             .orderId(paymentResponseAvroModel.getOrderId())
             .paymentId(paymentResponseAvroModel.getPaymentId())
             .price(paymentResponseAvroModel.getPrice())
             .failureMessages(paymentResponseAvroModel.getFailureMessages())
             .createdAt(Instant.ofEpochMilli(paymentResponseAvroModel.getCreatedAt()))
             .paymentStatus(PaymentStatus.valueOf(paymentResponseAvroModel.getPaymentStatus().name()))
             .build();
    }

    public RestaurantApprovalResponse approvalResponseAvroToApprovalResponse(RestaurantApprovalResponseAvroModel restaurantApprovalResponseAvroModel) {
        return RestaurantApprovalResponse.builder()
                .sagaId(restaurantApprovalResponseAvroModel.getSagaId())
                .orderId(restaurantApprovalResponseAvroModel.getOrderId())
                .createdAt(Instant.ofEpochMilli(restaurantApprovalResponseAvroModel.getCreatedAt()))
                .failureMessages(restaurantApprovalResponseAvroModel.getFailureMessages())
                .id(restaurantApprovalResponseAvroModel.getId())
                .restaurantId(restaurantApprovalResponseAvroModel.getRestaurantId())
                .orderApprovalStatus(OrderApprovalStatus.valueOf(restaurantApprovalResponseAvroModel.getOrderApprovalStatus().name()))
                .build();
    }
}
