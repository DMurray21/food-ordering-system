package com.food.ordering.system.restaurant.service.domain.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "restaurant-service")
@Data
public class RestaurantServiceConfig {

    private String restaurantApprovalRequestTopicName;
    private String restaurantApprovalResponseTopicName;
}
