package com.food.ordering.system.service.domain;

import com.food.ordering.system.order.service.domain.event.OrderCancelledEvent;
import com.food.ordering.system.service.domain.dto.message.RestaurantApprovalResponse;
import com.food.ordering.system.service.domain.ports.input.messagelistener.restaurantapproval.RestaurantApprovalResponseMessageListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
@Slf4j
public class RestaurantApprovalResponseMessageListenerImpl implements RestaurantApprovalResponseMessageListener {

    private final com.food.ordering.system.service.domain.OrderApprovalSaga orderApprovalSaga;

    public RestaurantApprovalResponseMessageListenerImpl(com.food.ordering.system.service.domain.OrderApprovalSaga orderApprovalSaga) {
        this.orderApprovalSaga = orderApprovalSaga;
    }

    @Override
    public void orderApproved(RestaurantApprovalResponse restaurantApprovalResponse) {
        orderApprovalSaga.process(restaurantApprovalResponse);
        log.info("Order approved {}", restaurantApprovalResponse.getOrderId());
    }

    @Override
    public void orderRejected(RestaurantApprovalResponse restaurantApprovalResponse) {
        OrderCancelledEvent orderCancelledEvent = orderApprovalSaga.rollback(restaurantApprovalResponse);
        log.info("Cancelled order {} with errors {}", restaurantApprovalResponse.getOrderId(), String.join(",", orderCancelledEvent.getOrder().getFailureMessages()));
        orderCancelledEvent.fire();

    }
}
