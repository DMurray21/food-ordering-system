package com.food.ordering.system.order.service.data.access.restaurant.adapter;

import com.food.ordering.system.order.service.data.access.restaurant.mapper.RestaurantDataMapper;
import com.food.ordering.system.order.service.data.access.restaurant.repository.RestaurantJpaRepository;
import com.food.ordering.system.order.service.domain.entity.Restaurant;
import com.food.ordering.system.service.domain.ports.output.repository.RestaurantRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class RestaurantRepositoryImpl implements RestaurantRepository {

    private final RestaurantJpaRepository restaurantJpaRepository;
    private final RestaurantDataMapper restaurantDataMapper;

    public RestaurantRepositoryImpl(RestaurantJpaRepository restaurantJpaRepository, RestaurantDataMapper restaurantDataMapper) {
        this.restaurantJpaRepository = restaurantJpaRepository;
        this.restaurantDataMapper = restaurantDataMapper;
    }

    @Override
    public Optional<Restaurant> findRestaurantInformation(Restaurant restaurant) {
        List<UUID> productIds = restaurantDataMapper.restaurantToRestaurantProducts(restaurant);
        return restaurantJpaRepository.findByRestaurantIdAndProductIdIn(restaurant.getId().getValue(), productIds)
                .map(restaurantDataMapper::restaurantEntityToRestaurant);
    }
}
