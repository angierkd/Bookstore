package com.shopping.book.cart.dto;

import lombok.Data;

@Data
public class UpdateCartDto {

    Long cartItemId;

    Long productId;

    int quantity;
}
