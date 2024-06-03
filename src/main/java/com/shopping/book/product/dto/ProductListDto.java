package com.shopping.book.product.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;

@Data
public class ProductListDto {

    Long id;
    String image;
    String name;
    String writer;
    String publisher;
    int price;

    @QueryProjection
    public ProductListDto(Long id, String image, String name, int price, String writer, String publisher) {
        this.id = id;
        this.image = image;
        this.name = name;
        this.writer = writer;
        this.publisher = publisher;
        this.price = price;

    }
}
