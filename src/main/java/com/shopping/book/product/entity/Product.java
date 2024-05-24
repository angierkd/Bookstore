package com.shopping.book.product.entity;

import jakarta.persistence.*;

@Entity
public class Product {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long id;

    private String name;

    private String image;

    private int stock;

    @ManyToOne
    @JoinColumn(name="category_id")
    private Category category;
}
