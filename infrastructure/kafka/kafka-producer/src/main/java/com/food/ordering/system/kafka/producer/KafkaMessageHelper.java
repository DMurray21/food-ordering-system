package com.food.ordering.system.kafka.producer;

import com.food.ordering.system.outbox.OutboxStatus;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.util.function.BiConsumer;

@Component
@Slf4j
public class KafkaMessageHelper {

    public <T, U> ListenableFutureCallback<SendResult<String, T>> getKafkaCallback(String topicName,
                                                                                   T avroModel,
                                                                                   String orderId,
                                                                                   String requestModelName,
                                                                                   U outboxMessage,
                                                                                   BiConsumer<U, OutboxStatus> outboxCallback) {

        return new ListenableFutureCallback<SendResult<String, T>>() {
            @Override
            public void onFailure(Throwable ex) {
                log.error("Error while sending {} message {} to topic {}", requestModelName, avroModel.toString(), topicName);
                outboxCallback.accept(outboxMessage, OutboxStatus.FAILED);
            }

            @Override
            public void onSuccess(SendResult<String, T> result) {
                RecordMetadata recordMetadata = result.getRecordMetadata();
                log.info("Received successful response from kafka for order id {}. Topic {} Partition {} Offset {} Timestamp {}",
                        orderId,
                        topicName,
                        recordMetadata.partition(),
                        recordMetadata.offset(),
                        recordMetadata.timestamp()
                );

                outboxCallback.accept(outboxMessage, OutboxStatus.COMPLETED);
            }
        };
    }

}
