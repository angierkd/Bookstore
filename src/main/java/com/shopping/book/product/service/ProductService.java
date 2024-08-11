package com.shopping.book.product.service;

import com.shopping.book.product.dto.ProductListDto;
import com.shopping.book.product.entity.Product;
import com.shopping.book.product.repository.ProductRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@AllArgsConstructor
@Service
public class ProductService {

    private ProductRepository productRepository;

    public Page<ProductListDto> getProductsByCategory(Pageable pageable, Long category, String sort){
        Page<ProductListDto> list = productRepository.searchProductsInCategory(pageable, category, sort);
        return list;
    }

    public Page<ProductListDto> getProductsBySearch(String searchQuery, String sort, Pageable pageable){
        Page<ProductListDto> list = productRepository.searchProductsInQuery(searchQuery, sort, pageable);
        return list;
    }

    public Product getProductDetails(Long id){
        Optional<Product> product = productRepository.findById(id);
        return product.orElse(null);
    }

    public void manageProduct() {
        // 상품 관리 비즈니스 로직
        if (true) { // 일부 조건이 실패할 경우 예외 발생
            throw new RuntimeException("Error managing product");
        }
    }

}
