package com.food.ordering.system.order.service.data.access.order.entity;

import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "order_items")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@IdClass(OrderItemEntityId.class)
public class OrderItemEntity {

    @Id
    private Long id;

    @Id
    @JoinColumn(name = "ORDER_ID")
    @ManyToOne(cascade = CascadeType.ALL)
    private OrderEntity order;

    private UUID productId;

    private Integer quantity;

    private BigDecimal price;

    private BigDecimal subTotal;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OrderItemEntity that = (OrderItemEntity) o;

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
