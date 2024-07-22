package com.shopping.book.cart.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@AllArgsConstructor
@Builder
@Data
public class UpdateCartDto {

    Long cartItemId;

    Long productId;

    int quantity;
}
