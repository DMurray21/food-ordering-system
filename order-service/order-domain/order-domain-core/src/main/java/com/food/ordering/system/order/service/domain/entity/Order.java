package com.food.ordering.system.order.service.domain.entity;

import com.food.ordering.system.domain.entity.AggregateRoot;
import com.food.ordering.system.domain.valueobject.*;
import com.food.ordering.system.order.service.domain.exception.OrderDomainException;
import com.food.ordering.system.order.service.domain.valueobject.OrderItemId;
import com.food.ordering.system.order.service.domain.valueobject.StreetAddress;
import com.food.ordering.system.order.service.domain.valueobject.TrackingId;

import java.util.List;
import java.util.UUID;

public class Order extends AggregateRoot<OrderId> {

    private final CustomerId customerId;
    private final RestaurantId restaurantId;
    private final StreetAddress streetAddress;
    private final  Money price;
    private final List<OrderItem> orderItems;
    private TrackingId trackingId;
    private OrderStatus orderStatus;
    private List<String> failureMessages;

    public void pay() {
        if(!OrderStatus.PENDING.equals(orderStatus)) {
            throw new OrderDomainException("Cannot pay for an order that isn't pending.");
        }

        orderStatus = OrderStatus.PAID;
    }

    public void approve() {
        if(!OrderStatus.PAID.equals(orderStatus)) {
            throw new OrderDomainException("Order must be in a paid state to be approved.");
        }

        orderStatus = OrderStatus.APPROVED;
    }

    public void initCancel(List<String> failureMessages) {
        if(!OrderStatus.PAID.equals(orderStatus)) {
            throw new OrderDomainException("Order must be in a paid state to be cancelled.");
        }

        orderStatus = OrderStatus.CANCELLING;
        updateFailureMessages(failureMessages);
    }

    public void cancel(List<String> failureMessages) {
        if(!(OrderStatus.PENDING.equals(orderStatus) || !OrderStatus.CANCELLING.equals(orderStatus))) {
            throw new OrderDomainException("Order must be in either a pending or cancelling status to be cancelled.");
        }

        orderStatus = OrderStatus.CANCELLED;
        updateFailureMessages(failureMessages);
    }

    private void updateFailureMessages(List<String> failureMessages) {
        if(this.failureMessages != null && failureMessages != null) {
            this.failureMessages.addAll(failureMessages.stream()
                    .filter(m -> !m.isEmpty())
                    .toList());
        }

        if(this.failureMessages == null) {
            this.failureMessages = failureMessages;
        }

    }

    public void initializeOrder() {
        setId(new OrderId(UUID.randomUUID()));
        trackingId = new TrackingId(UUID.randomUUID());
        orderStatus = OrderStatus.PENDING;
        initializeOrderItems();
    }

    public void validateOrder() {
        validateInitialOrder();
        validateTotalPrice();
        validateItemsPrice();
    }

    private void validateInitialOrder() {
        if(orderStatus != null || getId() != null) {
            throw new OrderDomainException("Order is not in the correct state for initialization");
        }
    }

    private void validateTotalPrice() {
        if(price == null || !price.isGreaterThanZero()) {
            throw new OrderDomainException("Total price must be greater than zero!");
        }
    }

    private void validateItemsPrice() {
        Money total = orderItems.stream()
                .map(orderItem -> {
                    validateItemPrice(orderItem);
                    return orderItem.getSubTotal();
                })
                .reduce(Money.ZERO, Money::add);

        if(!price.equals(total)) {
            throw new OrderDomainException("""
                    Total price: %s is not equal to order items total: %s
                    """.formatted(price.getAmount(), total.getAmount()));
        }
    }

    private void validateItemPrice(OrderItem orderItem) {
        if(!orderItem.isPriceValid()) {
            throw new OrderDomainException("""
                    Order item price: %s is not valid for product: %s
                    """.formatted(orderItem.getPrice(), orderItem.getProduct().getId()));
        }
    }

    private void initializeOrderItems() {
        long itemId = 1;
        for(OrderItem item: orderItems) {
            item.initializeOrderItem(this.getId(), new OrderItemId(itemId));
            itemId++;
        }
    }

    private Order(Builder builder) {
        super.setId(builder.orderId);
        customerId = builder.customerId;
        restaurantId = builder.restaurantId;
        streetAddress = builder.streetAddress;
        price = builder.price;
        orderItems = builder.orderItems;
        trackingId = builder.trackingId;
        orderStatus = builder.orderStatus;
        failureMessages = builder.failureMessages;
    }


    public CustomerId getCustomerId() {
        return customerId;
    }

    public RestaurantId getRestaurantId() {
        return restaurantId;
    }

    public StreetAddress getStreetAddress() {
        return streetAddress;
    }

    public Money getPrice() {
        return price;
    }

    public List<OrderItem> getOrderItems() {
        return orderItems;
    }

    public TrackingId getTrackingId() {
        return trackingId;
    }

    public OrderStatus getOrderStatus() {
        return orderStatus;
    }

    public List<String> getFailureMessages() {
        return failureMessages;
    }

    public static final class Builder {
        private OrderId orderId;
        private CustomerId customerId;
        private RestaurantId restaurantId;
        private StreetAddress streetAddress;
        private Money price;
        private List<OrderItem> orderItems;
        private TrackingId trackingId;
        private OrderStatus orderStatus;
        private List<String> failureMessages;

        private Builder() {
        }

        public static Builder builder() {
            return new Builder();
        }

        public Builder id(OrderId val) {
            orderId = val;
            return this;
        }

        public Builder customerId(CustomerId val) {
            customerId = val;
            return this;
        }

        public Builder restaurantId(RestaurantId val) {
            restaurantId = val;
            return this;
        }

        public Builder streetAddress(StreetAddress val) {
            streetAddress = val;
            return this;
        }

        public Builder price(Money val) {
            price = val;
            return this;
        }

        public Builder orderItems(List<OrderItem> val) {
            orderItems = val;
            return this;
        }

        public Builder trackingId(TrackingId val) {
            trackingId = val;
            return this;
        }

        public Builder orderStatus(OrderStatus val) {
            orderStatus = val;
            return this;
        }

        public Builder failureMessages(List<String> val) {
            failureMessages = val;
            return this;
        }

        public Order build() {
            return new Order(this);
        }
    }
}
