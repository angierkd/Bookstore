package com.shopping.book.order.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.shopping.book.order.entity.Orders;
import lombok.*;

@AllArgsConstructor
@Builder
@Setter
@Getter
@Data
public class OrderCompleteDto {
    private String impUid;
    private Orders order;
}
