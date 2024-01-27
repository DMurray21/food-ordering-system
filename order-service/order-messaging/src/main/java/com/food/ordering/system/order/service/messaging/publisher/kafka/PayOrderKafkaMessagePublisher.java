package com.food.ordering.system.order.service.messaging.publisher.kafka;

import com.food.ordering.system.domain.event.DomainEventPublisher;
import com.food.ordering.system.kafka.order.avro.model.RestaurantApprovalRequestAvroModel;
import com.food.ordering.system.kafka.producer.KafkaMessageHelper;
import com.food.ordering.system.kafka.producer.service.KafkaProducer;
import com.food.ordering.system.order.service.domain.event.OrderPaidEvent;
import com.food.ordering.system.order.service.messaging.mapper.OrderMessagingDataMapper;
import com.food.ordering.system.service.domain.OrderServiceConfigData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PayOrderKafkaMessagePublisher implements DomainEventPublisher<OrderPaidEvent> {

    private final OrderMessagingDataMapper orderMessagingDataMapper;
    private final KafkaProducer<String, RestaurantApprovalRequestAvroModel> kafkaProducer;
    private final OrderServiceConfigData orderServiceConfigData;
    private final KafkaMessageHelper kafkaMessageHelper;

    public PayOrderKafkaMessagePublisher(OrderMessagingDataMapper orderMessagingDataMapper, KafkaProducer<String, RestaurantApprovalRequestAvroModel> kafkaProducer, OrderServiceConfigData orderServiceConfigData, KafkaMessageHelper kafkaMessageHelper) {
        this.orderMessagingDataMapper = orderMessagingDataMapper;
        this.kafkaProducer = kafkaProducer;
        this.orderServiceConfigData = orderServiceConfigData;
        this.kafkaMessageHelper = kafkaMessageHelper;
    }

    @Override
    public void publish(OrderPaidEvent domainEvent) {
        String orderId = domainEvent.getOrder().getId().getValue().toString();

        RestaurantApprovalRequestAvroModel restaurantApprovalRequestAvroModel = orderMessagingDataMapper.orderPaidEventToRestaurantRequest(domainEvent);

        try {
            kafkaProducer.send(orderServiceConfigData.getRestaurantApprovalRequestTopicName(),
                    orderId,
                    restaurantApprovalRequestAvroModel,
                    kafkaMessageHelper.getKafkaCallback(orderServiceConfigData.getRestaurantApprovalResponseTopicName(), restaurantApprovalRequestAvroModel, orderId, "RestaurantApproval")
            );
        } catch (Exception e) {
            log.error("Error sending Restaurant approval message to kafka");
        }


        log.info("Restaurant approval for order: {}", orderId);

    }
}
