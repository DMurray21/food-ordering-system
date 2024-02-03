package com.food.ordering.system.order.service.messaging.publisher.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.food.ordering.system.kafka.order.avro.model.PaymentRequestAvroModel;
import com.food.ordering.system.kafka.producer.KafkaMessageHelper;
import com.food.ordering.system.kafka.producer.service.KafkaProducer;
import com.food.ordering.system.order.service.domain.exception.OrderDomainException;
import com.food.ordering.system.order.service.messaging.mapper.OrderMessagingDataMapper;
import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.service.domain.OrderServiceConfigData;
import com.food.ordering.system.service.domain.outbox.model.payment.OrderPaymentEventPayload;
import com.food.ordering.system.service.domain.outbox.model.payment.OrderPaymentOutboxMessage;
import com.food.ordering.system.service.domain.ports.output.messagepublisher.payment.PaymentRequestMessagePublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.function.BiConsumer;

@Component
@Slf4j
public class OrderPaymentEventKafkaPublisher implements PaymentRequestMessagePublisher {

    private final OrderMessagingDataMapper orderMessagingDataMapper;
    private final KafkaProducer<String, PaymentRequestAvroModel> kafkaProducer;
    private final OrderServiceConfigData orderServiceConfigData;
    private final KafkaMessageHelper kafkaMessageHelper;
    private final ObjectMapper objectMapper;

    public OrderPaymentEventKafkaPublisher(OrderMessagingDataMapper orderMessagingDataMapper, KafkaProducer<String, PaymentRequestAvroModel> kafkaProducer, OrderServiceConfigData orderServiceConfigData, KafkaMessageHelper kafkaMessageHelper, ObjectMapper objectMapper) {
        this.orderMessagingDataMapper = orderMessagingDataMapper;
        this.kafkaProducer = kafkaProducer;
        this.orderServiceConfigData = orderServiceConfigData;
        this.kafkaMessageHelper = kafkaMessageHelper;
        this.objectMapper = objectMapper;
    }

    @Override
    public void publish(OrderPaymentOutboxMessage orderPaymentOutboxMessage, BiConsumer<OrderPaymentOutboxMessage, OutboxStatus> outboxCallback) {
        OrderPaymentEventPayload orderPaymentEventPayload = getOrderPaymentEventPayload(orderPaymentOutboxMessage.getPayload());

        String sagaId = orderPaymentOutboxMessage.getSagaId().toString();

        log.info("Received OrderPaymentOutboxMessage for order id: {} and sagaId {}", orderPaymentEventPayload.getOrderId(), sagaId);

        PaymentRequestAvroModel paymentRequestAvroModel = orderMessagingDataMapper.orderPaymentEventToPaymentRequestAvroModel(sagaId, orderPaymentEventPayload);

        try {
            kafkaProducer.send(orderServiceConfigData.getPaymentRequestTopicName(),
                    sagaId,
                    paymentRequestAvroModel,
                    kafkaMessageHelper.getKafkaCallback(
                            orderServiceConfigData.getPaymentRequestTopicName(),
                            paymentRequestAvroModel,
                            orderPaymentEventPayload.getOrderId(),
                            "PaymentRequestAvrolModel",
                            orderPaymentOutboxMessage,
                            outboxCallback)
            );

            log.info("OrderPaymentEventPayload sent to kafka for order id: {}", orderPaymentEventPayload.getOrderId());
        } catch (Exception e) {
            log.error("Unabled to send OrderPaymentEventPayload to kafka for order id: {}", orderPaymentEventPayload.getOrderId());
        }
    }

    private OrderPaymentEventPayload getOrderPaymentEventPayload(String payload) {
        try {
            return objectMapper.readValue(payload, OrderPaymentEventPayload.class);
        } catch (JsonProcessingException e) {
            throw new OrderDomainException("Could not convert payload to event payload", e);
        }
    }


}
