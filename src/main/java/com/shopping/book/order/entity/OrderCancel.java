package com.shopping.book.order.entity;
import com.shopping.book.product.entity.Product;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.NoArgsConstructor;

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
    public OrderCancel(OrderProduct ordersProduct) {
        this.ordersProduct = ordersProduct;
    }
}
