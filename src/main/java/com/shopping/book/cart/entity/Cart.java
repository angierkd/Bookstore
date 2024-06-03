package com.shopping.book.cart.entity;

import com.shopping.book.product.entity.Product;
import com.shopping.book.user.entity.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@Entity
public class Cart {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name = "cart_id")
    private Long id;

    private int quantity;

    @ManyToOne
    @JoinColumn(name="user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name="product_id")
    private Product product;

    @Builder
    public Cart(int quantity, User user, Product product) {
        this.quantity = quantity;
        this.user = user;
        this.product = product;
    }
}
