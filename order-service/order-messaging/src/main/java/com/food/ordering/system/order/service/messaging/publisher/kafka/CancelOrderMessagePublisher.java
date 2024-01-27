package com.food.ordering.system.order.service.messaging.publisher.kafka;

import com.food.ordering.system.domain.event.DomainEventPublisher;
import com.food.ordering.system.kafka.order.avro.model.PaymentRequestAvroModel;
import com.food.ordering.system.kafka.producer.KafkaMessageHelper;
import com.food.ordering.system.kafka.producer.service.KafkaProducer;
import com.food.ordering.system.order.service.domain.event.OrderCancelledEvent;
import com.food.ordering.system.order.service.messaging.mapper.OrderMessagingDataMapper;
import com.food.ordering.system.service.domain.OrderServiceConfigData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CancelOrderMessagePublisher implements DomainEventPublisher<OrderCancelledEvent> {
    private final OrderMessagingDataMapper orderMessagingDataMapper;
    private final OrderServiceConfigData orderServiceConfigData;
    private final KafkaProducer<String, PaymentRequestAvroModel> kafkaProducer;
    private final KafkaMessageHelper kafkaMessageHelper;

    public CancelOrderMessagePublisher(OrderMessagingDataMapper orderMessagingDataMapper, OrderServiceConfigData orderServiceConfigData, KafkaProducer<String, PaymentRequestAvroModel> kafkaProducer, KafkaMessageHelper kafkaMessageHelper) {
        this.orderMessagingDataMapper = orderMessagingDataMapper;
        this.orderServiceConfigData = orderServiceConfigData;
        this.kafkaProducer = kafkaProducer;
        this.kafkaMessageHelper = kafkaMessageHelper;
    }

    @Override
    public void publish(OrderCancelledEvent domainEvent) {
        String orderId = domainEvent.getOrder().getId().getValue().toString();
        log.info("Received order cancelled event for order id {}", orderId);

        PaymentRequestAvroModel paymentRequestAvroModel = orderMessagingDataMapper.orderCancelledEventToPaymentRequestAvroModel(domainEvent);
        try {
            kafkaProducer.send(orderServiceConfigData.getPaymentRequestTopicName(),
                    orderId,
                    paymentRequestAvroModel,
                    kafkaMessageHelper.getKafkaCallback(orderServiceConfigData.getPaymentRequestTopicName(), paymentRequestAvroModel, orderId, "PaymentRequest")
            );
        } catch (Exception e) {
            log.info("Error while sending payment request to kafka for order id {} with error {}", orderId, e.getMessage());
        }


        log.info("Payment request sent to kafka for order id {}", orderId);


    }

}
