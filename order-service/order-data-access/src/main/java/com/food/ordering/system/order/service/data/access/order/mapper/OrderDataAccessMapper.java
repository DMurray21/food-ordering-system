package com.food.ordering.system.order.service.data.access.order.mapper;

import com.food.ordering.system.domain.valueobject.*;
import com.food.ordering.system.order.service.data.access.order.entity.OrderAddressEntity;
import com.food.ordering.system.order.service.data.access.order.entity.OrderEntity;
import com.food.ordering.system.order.service.data.access.order.entity.OrderItemEntity;
import com.food.ordering.system.order.service.domain.entity.Order;
import com.food.ordering.system.order.service.domain.entity.OrderItem;
import com.food.ordering.system.order.service.domain.entity.Product;
import com.food.ordering.system.order.service.domain.valueobject.OrderItemId;
import com.food.ordering.system.order.service.domain.valueobject.StreetAddress;
import com.food.ordering.system.order.service.domain.valueobject.TrackingId;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class OrderDataAccessMapper {

    public OrderEntity orderToOrderEntity(Order order) {
        OrderEntity orderEntity = OrderEntity.builder()
                .id(order.getId().getValue())
                .trackingId(order.getTrackingId().getValue())
                .customerId(order.getCustomerId().getValue())
                .restaurantId(order.getRestaurantId().getValue())
                .orderAddressEntity(deliveryAddressToEntity(order.getStreetAddress()))
                .price(order.getPrice().getAmount())
                .orderItems(orderItemsToOrderItemEntity(order.getOrderItems()))
                .orderStatus(order.getOrderStatus())
                .failureMessages(order.getFailureMessages() != null ? String.join(",", order.getFailureMessages()) : "")
                .build();

        orderEntity.getOrderAddressEntity().setOrder(orderEntity);
        orderEntity.getOrderItems().forEach(i -> i.setOrder(orderEntity));

        return orderEntity;
    }

    private List<OrderItemEntity> orderItemsToOrderItemEntity(List<OrderItem> orderItems) {
        return orderItems.stream()
                .map(i -> OrderItemEntity.builder()
                        .id(i.getId().getValue())
                        .price(i.getPrice().getAmount())
                        .quantity(i.getQuantity())
                        .subTotal(i.getSubTotal().getAmount())
                        .productId(i.getProduct().getId().getValue())
                        .build())
                .toList();
    }

    private OrderAddressEntity deliveryAddressToEntity(StreetAddress streetAddress) {
        return OrderAddressEntity.builder()
                .id(streetAddress.getId())
                .city(streetAddress.getCity())
                .postalCode(streetAddress.getPostalCode())
                .street(streetAddress.getStreet())
                .build();
    }

    public Order orderEntityToOrder(OrderEntity orderEntity) {
        return Order.Builder.builder()
                .id(new OrderId(orderEntity.getId()))
                .customerId(new CustomerId(orderEntity.getCustomerId()))
                .restaurantId(new RestaurantId(orderEntity.getRestaurantId()))
                .trackingId(new TrackingId(orderEntity.getTrackingId()))
                .price(new Money(orderEntity.getPrice()))
                .streetAddress(addressEntityToAddress(orderEntity.getOrderAddressEntity()))
                .orderItems(orderItemEntityToOrderItems(orderEntity.getOrderItems()))
                .trackingId(new TrackingId(orderEntity.getTrackingId()))
                .orderStatus(orderEntity.getOrderStatus())
                .failureMessages(!orderEntity.getFailureMessages().isEmpty() ?
                        new ArrayList<>(Arrays.asList(orderEntity.getFailureMessages().split(","))):
                        new ArrayList<>()
                )
                .build();
    }

    private List<OrderItem> orderItemEntityToOrderItems(List<OrderItemEntity> orderItems) {
        return orderItems.stream()
                .map(i -> OrderItem.Builder.builder()
                        .orderId(new OrderId(i.getOrder().getId()))
                        .price(new Money(i.getPrice()))
                        .quantity(i.getQuantity())
                        .subTotal(new Money(i.getSubTotal()))
                        .product(new Product(new ProductId(i.getProductId())))
                        .orderItemId(new OrderItemId(i.getId()))
                        .build())
                .toList();
    }

    private StreetAddress addressEntityToAddress(OrderAddressEntity orderAddressEntity) {
        return new StreetAddress(orderAddressEntity.getId(), orderAddressEntity.getStreet(), orderAddressEntity.getPostalCode(), orderAddressEntity.getCity());
    }
}
