package com.shopping.book.order.dto;

import com.shopping.book.cart.entity.Cart;
import com.shopping.book.order.entity.OrderProduct;
import com.shopping.book.order.entity.Orders;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Builder
@AllArgsConstructor
@Data
public class OrderCreateDto {

    private String name;
    private String address;
    private String phoneNum;

    //이거 리스트여야하는디
    private List<Long> cartIds;

    public Orders convertToOrders(List<Cart> carts) {
        return Orders.builder()
                .name(this.name)
                .address(this.address)
                .phoneNum(this.phoneNum)
                .date(LocalDateTime.now())
                .status(false)
                .totalPrice(calculateTotalPrice(carts))
                .user(carts.get(0).getUser())
                .build();
    }

    private int calculateTotalPrice(List<Cart> carts) {
        return carts.stream()
                .mapToInt(cart -> cart.getProduct().getPrice() * cart.getQuantity())
                .sum();
    }

    public List<OrderProduct> convertToOrderProduct(List<Cart> carts, Orders order){
        // cartid로 Cart를 가져와서 Update
        return carts.stream()
                .map(cart -> OrderProduct.builder().quantity(cart.getQuantity())
                     .status(false) // 결제취소여부
                     .orders(order)
                     .product(cart.getProduct())
                     .build())
                .collect(Collectors.toList());
    }
}
