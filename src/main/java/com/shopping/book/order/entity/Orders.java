package com.shopping.book.order.entity;

import com.shopping.book.user.entity.User;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class Orders {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name = "orders_id")
    private Long id;

    private String name;
    private String address;
    private String phoneNum;
    private LocalDateTime date;
    private Boolean status; // 결제대기, 결제완료
    private int totalPrice;

    @ManyToOne
    @JoinColumn(name="user_id")
    private User user;
}
