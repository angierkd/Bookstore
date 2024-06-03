package com.shopping.book.order.entity;

import com.shopping.book.user.entity.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
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

    @Builder
    public Orders(String name, String address, String phoneNum, LocalDateTime date, Boolean status, int totalPrice, User user) {
        this.name = name;
        this.address = address;
        this.phoneNum = phoneNum;
        this.date = date;
        this.status = status;
        this.totalPrice = totalPrice;
        this.user = user;
    }
}
