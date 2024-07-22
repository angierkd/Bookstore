package com.shopping.book.product.repository;

import com.shopping.book.product.dto.ProductListDto;
import com.shopping.book.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ProductRepositoryCustom {

   Page<ProductListDto> searchProductsInCategory(Pageable pageable, Long categoryId, String sort);
   Page<ProductListDto> searchProductsInQuery(String searchQuery, String sort, Pageable pageable);

   Optional<Product> findByIdWithPessimisticLock(Long id);
}
