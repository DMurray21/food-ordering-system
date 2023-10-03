package com.food.ordering.system.service.domain.mapper;

import com.food.ordering.system.domain.valueobject.CustomerId;
import com.food.ordering.system.domain.valueobject.Money;
import com.food.ordering.system.domain.valueobject.ProductId;
import com.food.ordering.system.domain.valueobject.RestaurantId;
import com.food.ordering.system.order.service.domain.entity.Order;
import com.food.ordering.system.order.service.domain.entity.OrderItem;
import com.food.ordering.system.order.service.domain.entity.Product;
import com.food.ordering.system.order.service.domain.entity.Restaurant;
import com.food.ordering.system.order.service.domain.valueobject.StreetAddress;
import com.food.ordering.system.service.domain.dto.create.CreateOrderCommand;
import com.food.ordering.system.service.domain.dto.create.CreateOrderResponse;
import com.food.ordering.system.service.domain.dto.track.TrackOrderResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class OrderDataMapper {

    public Restaurant createOrderCommandToRestaurant(CreateOrderCommand createOrderCommand) {
        List<Product> products = createOrderCommand.getItems()
                .stream()
                .map(i -> new Product(new ProductId(i.getProductId())))
                .toList();

        return Restaurant.Builder.builder()
                .id(new RestaurantId(createOrderCommand.getRestaurantId()))
                .products(products)
                .build();
    }

    public Order createOrderCommandToOrder(CreateOrderCommand createOrderCommand) {
        return Order.Builder.builder()
                .customerId(new CustomerId(createOrderCommand.getCustomerId()))
                .restaurantId(new RestaurantId(createOrderCommand.getRestaurantId()))
                .streetAddress(orderAddressToStreetAddress(createOrderCommand.getAddress()))
                .price(new Money(createOrderCommand.getPrice()))
                .orderItems(orderItemsToOrderItemEntities(createOrderCommand.getItems()))
                .build();
    }

    private StreetAddress orderAddressToStreetAddress(com.food.ordering.system.service.domain.dto.create.StreetAddress streetAddress) {
        return new StreetAddress(UUID.randomUUID(), streetAddress.getStreet(), streetAddress.getPostalCode(), streetAddress.getCity());
    }

    private List<OrderItem> orderItemsToOrderItemEntities(List<com.food.ordering.system.service.domain.dto.create.OrderItem> items) {
        return items.stream().map(i -> com.food.ordering.system.order.service.domain.entity.OrderItem.Builder.builder()
                        .product(new Product(new ProductId(i.getProductId())))
                        .price(new Money(i.getPrice()))
                        .quantity(i.getQuantity())
                        .subTotal(new Money(i.getSubTotal()))
                        .build()
                )
                .toList();
    }

    public CreateOrderResponse orderToCreateOrderResponse(Order order, String message) {
        return CreateOrderResponse.builder()
                .orderTrackingId(order.getTrackingId().getValue())
                .orderStatus(order.getOrderStatus())
                .message(message)
                .build();
    }


    public TrackOrderResponse orderToTrackOrderResponse(Order order) {
        return TrackOrderResponse.builder()
                .orderStatus(order.getOrderStatus())
                .orderTrackingId(order.getTrackingId().getValue())
                .failureMessages(order.getFailureMessages())
                .build();
    }
}
