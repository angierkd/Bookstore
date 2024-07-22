package com.shopping.book.order.entity;
import com.shopping.book.product.entity.Product;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
public class OrderCancel {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name = "order_cancel_id")
    private Long id;

    @OneToOne
    @JoinColumn(name="order_product_id")
    private OrderProduct ordersProduct;

    @Builder
    public OrderCancel(Long id, OrderProduct ordersProduct) {
        this.id = id;
        this.ordersProduct = ordersProduct;
    }
}
