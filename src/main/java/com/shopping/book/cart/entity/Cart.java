package com.shopping.book.cart.entity;

import com.shopping.book.product.entity.Product;
import com.shopping.book.user.entity.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
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
    public Cart(Long id, int quantity, User user, Product product) {
        this.id = id;
        this.quantity = quantity;
        this.user = user;
        this.product = product;
    }
}
