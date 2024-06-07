package com.shopping.book.product.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
public class Product {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long id;

    private String name;

    private String image;

    private String writer;

    private String publisher;

    private int price;

    private int stock;

    @ManyToOne
    @JoinColumn(name="category_id")
    private Category category;

    // 빌더 패턴을 위한 내부 Builder 클래스
    @Builder
    public Product(Long id, String name, String image, int price, int stock, Category category, String writer, String publisher) {
        this.id = id;
        this.name = name;
        this.image = image;
        this.price = price;
        this.writer = writer;
        this.publisher = publisher;
        this.stock = stock;
        this.category = category;
    }

}
