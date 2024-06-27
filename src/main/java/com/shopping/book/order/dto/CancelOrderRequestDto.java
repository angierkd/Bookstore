package com.shopping.book.order.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class CancelOrderRequestDto {

    private Long orderId;
    private Long orderProductId;
    private int amount;
    private String reason;

}
