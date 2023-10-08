package com.food.ordering.system.restaurant.service.domain.entity;

import com.food.ordering.system.domain.entity.AggregateRoot;
import com.food.ordering.system.domain.valueobject.*;
import com.food.ordering.system.restaurant.service.domain.valueobject.OrderApprovalId;

import java.util.List;
import java.util.UUID;

public class Restaurant extends AggregateRoot<RestaurantId> {

    private OrderApproval orderApproval;
    private boolean active;
    private OrderDetail orderDetail;

    public void validateOrder(List<String> failureMessages) {

        if(orderDetail.getOrderStatus() == OrderStatus.PAID) {
            failureMessages.add("Payment is not completed for order");
        }

        Money totalAmount = orderDetail.getProducts().stream()
                .map(p -> {
                    if (!p.isAvailable()) {
                        failureMessages.add("Product is not available " + p.getId().getValue());
                    }

                    return p.getPrice().multiply(p.getQuantity());
                }).reduce(Money.ZERO, Money::add);

        if(!totalAmount.equals(orderDetail.getTotalAmount())) {
            failureMessages.add("Order total is not correct");
        }
    }

    public void constructOrderApproval(OrderApprovalStatus orderApprovalStatus) {
        this.orderApproval = OrderApproval.Builder.builder()
                .orderId(this.getOrderDetail().getId())
                .restaurantId(this.getId())
                .id(new OrderApprovalId(UUID.randomUUID()))
                .orderApprovalStatus(orderApprovalStatus)
                .build();
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    private Restaurant(Builder builder) {
        setId(builder.id);
        orderApproval = builder.orderApproval;
        active = builder.active;
        orderDetail = builder.orderDetail;
    }

    public OrderApproval getOrderApproval() {
        return orderApproval;
    }

    public boolean isActive() {
        return active;
    }

    public OrderDetail getOrderDetail() {
        return orderDetail;
    }


    public static final class Builder {
        private RestaurantId id;
        private OrderApproval orderApproval;
        private boolean active;
        private OrderDetail orderDetail;

        private Builder() {
        }

        public static Builder builder() {
            return new Builder();
        }

        public Builder id(RestaurantId val) {
            id = val;
            return this;
        }

        public Builder orderApproval(OrderApproval val) {
            orderApproval = val;
            return this;
        }

        public Builder active(boolean val) {
            active = val;
            return this;
        }

        public Builder orderDetail(OrderDetail val) {
            orderDetail = val;
            return this;
        }

        public Restaurant build() {
            return new Restaurant(this);
        }
    }
}
