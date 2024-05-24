package com.shopping.book.user.entity;

import jakarta.persistence.*;

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
}
