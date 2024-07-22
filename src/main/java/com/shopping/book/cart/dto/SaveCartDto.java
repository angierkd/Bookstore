package com.shopping.book.cart.dto;

import com.shopping.book.cart.entity.Cart;
import com.shopping.book.product.entity.Product;
import com.shopping.book.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@AllArgsConstructor
@Data
public class SaveCartDto {

    private int quantity;

    private Long userId;

    private Long productId;

    public static Cart convertToEntity(int quantity, User user, Product product) {
        return Cart.builder()
                .quantity(quantity)
                .user(user)
                .product(product)
                .build();
    }
}
