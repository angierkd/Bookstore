package com.shopping.book.user.entity;

import com.shopping.book.order.entity.OrderProduct;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Entity
public class User {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    private String uid;

    private String password;

    private String socialProvider;

    private String role;

    @Builder
    public User(String uid, String password, String socialProvider, String role) {
        //this.id = id;
        this.uid = uid;
        this.password = password;
        this.socialProvider = socialProvider;
        this.role = role;
    }
}
