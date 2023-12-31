package com.food.ordering.system.order.service.data.access.order.entity;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderItemEntityId implements Serializable {

    private Long id;

    private OrderEntity order;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OrderItemEntityId that = (OrderItemEntityId) o;

        if (!id.equals(that.id)) return false;
        return order.equals(that.order);
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + order.hashCode();
        return result;
    }
}
