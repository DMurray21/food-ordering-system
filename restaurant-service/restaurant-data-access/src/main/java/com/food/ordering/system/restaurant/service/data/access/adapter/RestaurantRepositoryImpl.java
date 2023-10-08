package com.food.ordering.system.restaurant.service.data.access.adapter;

import com.food.ordering.system.data.access.restaurant.repository.RestaurantJpaRepository;
import com.food.ordering.system.restaurant.service.data.access.mapper.RestaurantDataAccessMapper;
import com.food.ordering.system.restaurant.service.domain.entity.Product;
import com.food.ordering.system.restaurant.service.domain.entity.Restaurant;
import com.food.ordering.system.restaurant.service.domain.ports.output.repository.RestaurantRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class RestaurantRepositoryImpl implements RestaurantRepository {

    private final RestaurantJpaRepository restaurantJpaRepository;
    private final RestaurantDataAccessMapper restaurantDataAccessMapper;

    public RestaurantRepositoryImpl(RestaurantJpaRepository restaurantJpaRepository, RestaurantDataAccessMapper restaurantDataAccessMapper) {
        this.restaurantJpaRepository = restaurantJpaRepository;
        this.restaurantDataAccessMapper = restaurantDataAccessMapper;
    }


    @Override
    public Optional<Restaurant> findRestaurantInformation(Restaurant restaurant) {
        List<UUID> productIds = restaurant.getOrderDetail().getProducts().stream().map(p -> p.getId().getValue()).toList();

        return restaurantJpaRepository.findByRestaurantIdAndProductIdIn(restaurant.getId().getValue(), productIds).map(restaurantDataAccessMapper::restaurantEntityToRestaurant);
    }
}
