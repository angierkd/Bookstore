package com.shopping.book.order.entity;

import com.shopping.book.user.entity.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Setter
@Getter
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

    @OneToMany(mappedBy = "orders")
    private List<OrderProduct> orderProducts;

    @Builder
    public Orders(Long id, String name, String address, String phoneNum, LocalDateTime date, Boolean status, int totalPrice, User user) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.phoneNum = phoneNum;
        this.date = date;
        this.status = status;
        this.totalPrice = totalPrice;
        this.user = user;
    }

}
