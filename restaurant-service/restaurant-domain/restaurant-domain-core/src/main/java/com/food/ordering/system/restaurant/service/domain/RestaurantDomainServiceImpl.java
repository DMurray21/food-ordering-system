package com.food.ordering.system.restaurant.service.domain;

import com.food.ordering.system.domain.event.DomainEventPublisher;
import com.food.ordering.system.domain.valueobject.OrderApprovalStatus;
import com.food.ordering.system.restaurant.service.domain.entity.Restaurant;
import com.food.ordering.system.restaurant.service.domain.event.OrderApprovalEvent;
import com.food.ordering.system.restaurant.service.domain.event.OrderApprovedEvent;
import com.food.ordering.system.restaurant.service.domain.event.OrderRejectedEvent;
import lombok.extern.slf4j.Slf4j;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;

@Slf4j
public class RestaurantDomainServiceImpl implements RestaurantDomainService{

    @Override
    public OrderApprovalEvent validateOrder(Restaurant restaurant, List<String> failureMessages, DomainEventPublisher<OrderApprovedEvent> orderApprovedEventDomainEventPublisher, DomainEventPublisher<OrderRejectedEvent> orderRejectedEventDomainEventPublisher) {
        restaurant.validateOrder(failureMessages);
        log.info("validating order with id {}", restaurant.getOrderDetail().getId().getValue());
        if(failureMessages.isEmpty()) {
            log.info("Approving order with id {}", restaurant.getOrderDetail().getId().getValue());
            restaurant.constructOrderApproval(OrderApprovalStatus.APPROVED);
            return new OrderApprovedEvent(restaurant.getOrderApproval(), restaurant.getId(), failureMessages, ZonedDateTime.now(ZoneOffset.UTC), orderApprovedEventDomainEventPublisher);
        }

        log.info("Order could not be validated {}", restaurant.getOrderDetail().getId().getValue());
        restaurant.constructOrderApproval(OrderApprovalStatus.REJECTED);
        return new OrderRejectedEvent(restaurant.getOrderApproval(), restaurant.getId(), failureMessages, ZonedDateTime.now(ZoneOffset.UTC), orderRejectedEventDomainEventPublisher);
    }
}
