package com.shopping.book.product.entity;

import jakarta.persistence.*;

@Entity
public class Category {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Long id;

    private String name;

    @ManyToOne
    @JoinColumn(name="parent_category_id")
    private Category parentCategory;

}
