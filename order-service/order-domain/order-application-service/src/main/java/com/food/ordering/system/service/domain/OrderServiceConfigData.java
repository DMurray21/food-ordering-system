package com.food.ordering.system.service.domain;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
@ConfigurationProperties(prefix = "order-service")
public class OrderServiceConfigData {

    private String paymentRequestTopicName;
    private String paymentResponseTopicName;
    private String restaurantApprovalRequestTopicName;
    private String restaurantApprovalResponseTopicName;
}
