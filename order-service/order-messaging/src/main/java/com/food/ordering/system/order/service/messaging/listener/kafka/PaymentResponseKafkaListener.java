package com.food.ordering.system.order.service.messaging.listener.kafka;

import com.food.ordering.system.kafka.consumer.KafkaConsumer;
import com.food.ordering.system.kafka.order.avro.model.PaymentResponseAvroModel;
import com.food.ordering.system.kafka.order.avro.model.PaymentStatus;
import com.food.ordering.system.order.service.messaging.mapper.OrderMessagingDataMapper;
import com.food.ordering.system.service.domain.ports.input.messagelistener.payment.PaymentResponseMessageListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class PaymentResponseKafkaListener implements KafkaConsumer<PaymentResponseAvroModel> {


    private final PaymentResponseMessageListener paymentResponseMessageListener;
    private final OrderMessagingDataMapper orderMessagingDataMapper;

    public PaymentResponseKafkaListener(PaymentResponseMessageListener paymentResponseMessageListener, OrderMessagingDataMapper orderMessagingDataMapper) {
        this.paymentResponseMessageListener = paymentResponseMessageListener;
        this.orderMessagingDataMapper = orderMessagingDataMapper;
    }

    @Override
    @KafkaListener(id = "${kafka-consumer-config.payment-consumer-group-id}", topics = "${order-service.payment-response-topic=name}")
    public void receive(@Payload List<PaymentResponseAvroModel> messages,
                        @Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) List<String> keys,
                        @Header(KafkaHeaders.RECEIVED_PARTITION_ID) List<Integer> partitions,
                        @Header(KafkaHeaders.OFFSET) List<Long> offsets) {

        log.info("{} number of payment responses received with keys {}, partitions {} and offsets: {}",
                messages.size(), keys.toString(), partitions.toString(), offsets.toString());

        messages.forEach(p -> {
            if(PaymentStatus.COMPLETED == p.getPaymentStatus()) {
                log.info("Processing successful payment for order {}", p.getOrderId());
                paymentResponseMessageListener.paymentCompleted(orderMessagingDataMapper.paymentResponseAvroModelToPaymentResponse(p));
            } else if (PaymentStatus.CANCELLED == p.getPaymentStatus() || PaymentStatus.FAILED == p.getPaymentStatus()) {
                log.info("Processing unsuccessful for payment with order {}", p.getOrderId());
                paymentResponseMessageListener.paymentCancelled(orderMessagingDataMapper.paymentResponseAvroModelToPaymentResponse(p));
            }
        });

    }
}
