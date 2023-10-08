package com.food.ordering.system.order.service.data.access.restaurant.mapper;

import com.food.ordering.system.data.access.restaurant.entity.RestaurantEntity;
import com.food.ordering.system.data.access.restaurant.exception.RestaurantDataAccessException;
import com.food.ordering.system.domain.valueobject.Money;
import com.food.ordering.system.domain.valueobject.ProductId;
import com.food.ordering.system.domain.valueobject.RestaurantId;
import com.food.ordering.system.order.service.domain.entity.Product;
import com.food.ordering.system.order.service.domain.entity.Restaurant;
import org.springframework.stereotype.Component;

import java.nio.file.ReadOnlyFileSystemException;
import java.util.List;
import java.util.UUID;

@Component
public class RestaurantDataMapper {

    public List<UUID> restaurantToRestaurantProducts(Restaurant restaurant) {
        return restaurant.getProducts().stream().map(p -> p.getId().getValue()).toList();
    }

    public Restaurant restaurantEntityToRestaurant(List<RestaurantEntity> restaurantEntities) {
        RestaurantEntity restaurantEntity = restaurantEntities.stream()
                .findFirst()
                .orElseThrow(() -> new RestaurantDataAccessException("Restaurant could not be found"));

        List<Product> products = restaurantEntities.stream()
                .map(r -> new Product(new ProductId(r.getProductId()), r.getProductName(), new Money(r.getProductPrice())))
                .toList();

        return Restaurant.Builder.builder()
                .products(products)
                .id(new RestaurantId(restaurantEntity.getRestaurantId()))
                .active(restaurantEntity.isRestaurantActive())
                .build();
    }
}
