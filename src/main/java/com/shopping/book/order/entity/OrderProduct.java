package com.shopping.book.order.entity;
import com.shopping.book.product.entity.Product;
import com.shopping.book.user.entity.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@Entity
public class OrderProduct {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name = "order_product_id")
    private Long id;

    private int quantity;
    private Boolean status;  // 주문, 주문취소

    @ManyToOne
    @JoinColumn(name="orders_id")
    private Orders orders;

    @ManyToOne
    @JoinColumn(name="product_id")
    private Product product;

    @Builder
    public OrderProduct(Long id, int quantity, Boolean status, Orders orders, Product product) {
        this.id = id;
        this.quantity = quantity;
        this.status = status;
        this.orders = orders;
        this.product = product;
    }
}
