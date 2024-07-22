package com.shopping.book.product.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class ProductListDto {

    Long id;
    String image;
    String name;
    String writer;
    String publisher;
    int price;
    int quantity;

    @QueryProjection
    public ProductListDto(Long id, String image, String name, int price, String writer, String publisher,int quantity) {
        this.id = id;
        this.image = image;
        this.name = name;
        this.writer = writer;
        this.publisher = publisher;
        this.price = price;
        this.quantity = quantity;
    }

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
