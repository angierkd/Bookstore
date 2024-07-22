package com.shopping.book.user.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
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
    public User(Long id, String uid, String password, String socialProvider, String role) {
        this.id = id;
        this.uid = uid;
        this.password = password;
        this.socialProvider = socialProvider;
        this.role = role;
    }
}
