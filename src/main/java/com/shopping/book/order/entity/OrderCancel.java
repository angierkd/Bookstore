package com.shopping.book.order.entity;
import jakarta.persistence.*;

@Entity
public class OrderCancel {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name = "order_cancel_id")
    private Long id;

    @OneToOne
    @JoinColumn(name="order_product_id")
    private OrderProduct ordersProduct;
}
