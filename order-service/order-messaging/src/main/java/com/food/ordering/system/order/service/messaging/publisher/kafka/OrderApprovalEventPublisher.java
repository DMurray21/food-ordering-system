package com.food.ordering.system.order.service.messaging.publisher.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.food.ordering.system.kafka.order.avro.model.PaymentRequestAvroModel;
import com.food.ordering.system.kafka.order.avro.model.RestaurantApprovalRequestAvroModel;
import com.food.ordering.system.kafka.producer.KafkaMessageHelper;
import com.food.ordering.system.kafka.producer.service.KafkaProducer;
import com.food.ordering.system.order.service.domain.exception.OrderDomainException;
import com.food.ordering.system.order.service.messaging.mapper.OrderMessagingDataMapper;
import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.service.domain.OrderServiceConfigData;
import com.food.ordering.system.service.domain.outbox.model.approval.OrderApprovalEventPayload;
import com.food.ordering.system.service.domain.outbox.model.approval.OrderApprovalOutboxMessage;
import com.food.ordering.system.service.domain.outbox.model.payment.OrderPaymentEventPayload;
import com.food.ordering.system.service.domain.ports.output.messagepublisher.restaurantapproval.RestaurantApprovalRequestMessagePublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.function.BiConsumer;

@Slf4j
@Component
public class OrderApprovalEventPublisher implements RestaurantApprovalRequestMessagePublisher {

    private final OrderMessagingDataMapper orderMessagingDataMapper;
    private final KafkaProducer<String, RestaurantApprovalRequestAvroModel> kafkaProducer;
    private final OrderServiceConfigData orderServiceConfigData;
    private final KafkaMessageHelper kafkaMessageHelper;
    private final ObjectMapper objectMapper;

    public OrderApprovalEventPublisher(OrderMessagingDataMapper orderMessagingDataMapper, KafkaProducer<String, RestaurantApprovalRequestAvroModel> kafkaProducer, OrderServiceConfigData orderServiceConfigData, KafkaMessageHelper kafkaMessageHelper, ObjectMapper objectMapper) {
        this.orderMessagingDataMapper = orderMessagingDataMapper;
        this.kafkaProducer = kafkaProducer;
        this.orderServiceConfigData = orderServiceConfigData;
        this.kafkaMessageHelper = kafkaMessageHelper;
        this.objectMapper = objectMapper;
    }


    @Override
    public void publish(OrderApprovalOutboxMessage orderApprovalOutboxMessage, BiConsumer<OrderApprovalOutboxMessage, OutboxStatus> outboxCallback) {
        OrderApprovalEventPayload orderApprovalEventPayload = getOrderApprovalEventPayload(orderApprovalOutboxMessage.getPayload());

        String sagaId = orderApprovalOutboxMessage.getSagaId().toString();

        log.info("Received OrderPaymentOutboxMessage for order id: {} and sagaId {}", orderApprovalEventPayload.getOrderId(), sagaId);

        RestaurantApprovalRequestAvroModel restaurantApprovalRequestAvroModel = orderMessagingDataMapper.orderApprovalEventToApprovalRequestAvroModel(sagaId, orderApprovalEventPayload);

        try {
            kafkaProducer.send(orderServiceConfigData.getRestaurantApprovalRequestTopicName(),
                    sagaId,
                    restaurantApprovalRequestAvroModel,
                    kafkaMessageHelper.getKafkaCallback(
                            orderServiceConfigData.getRestaurantApprovalResponseTopicName(),
                            restaurantApprovalRequestAvroModel,
                            orderApprovalEventPayload.getOrderId(),
                            "RestaurantApprovalRequestAvroModel",
                            orderApprovalOutboxMessage,
                            outboxCallback)
            );

            log.info("OrderPaymentEventPayload sent to kafka for order id: {}", orderApprovalEventPayload.getOrderId());
        } catch (Exception e) {
            log.error("Unabled to send OrderApprovalEventPayload to kafka for order id: {}", orderApprovalEventPayload.getOrderId());
        }
    }

    private OrderApprovalEventPayload getOrderApprovalEventPayload(String payload) {
        try {
            return objectMapper.readValue(payload, OrderApprovalEventPayload.class);
        } catch (JsonProcessingException e) {
            throw new OrderDomainException("Could not convert payload to event payload", e);
        }
    }
}
