package com.shopping.book.product.service;

import com.shopping.book.cart.repository.CartRepository;
import com.shopping.book.order.repository.OrderCancelRepository;
import com.shopping.book.order.repository.OrderProductRepository;
import com.shopping.book.order.repository.OrdersRepository;
import com.shopping.book.order.service.OrderService;
import com.shopping.book.product.dto.ProductListDto;
import com.shopping.book.product.entity.Product;
import com.shopping.book.product.repository.ProductRepository;
import com.siot.IamportRestClient.IamportClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    @DisplayName("카테고리로 상품 검색 테스트")
    public void getProductsByCategoryTest(){
        // given
        ProductListDto productListDto = new ProductListDto();
        List<ProductListDto> productList = List.of(productListDto);

        Pageable pageable = PageRequest.of(0, 3);
        Page<ProductListDto> page = new PageImpl<>(productList, pageable, productList.size());

        when(productRepository.searchProductsInCategory(any(Pageable.class), anyLong(), anyString())).thenReturn(page);

        // when
        Page<ProductListDto> result = productService.getProductsByCategory(pageable, 1L, "popularity");

        // then
        assertThat(result).isEqualTo(page);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0)).isEqualTo(productListDto);
    }

    @Test
    @DisplayName("검색어로 상품 검색 테스트")
    public void getProductsBySearchTest() {
        // given
        ProductListDto productListDto = new ProductListDto();
        productListDto.setName("Test Product");
        List<ProductListDto> productList = List.of(productListDto);

        Pageable pageable = PageRequest.of(0, 3);
        Page<ProductListDto> page = new PageImpl<>(productList, pageable, productList.size());

        when(productRepository.searchProductsInQuery(anyString(), anyString(), any(Pageable.class))).thenReturn(page);

        // when
        Page<ProductListDto> result = productService.getProductsBySearch("소설", "popularity", pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Test Product");
    }

    @Test
    @DisplayName("상품 상세 조회 테스트")
    public void getProductDetailsTest(){
        //given
        Product product = Product.builder().id(1L).build();

        when(productRepository.findById(product.getId())).thenReturn(Optional.ofNullable(product));

        //when
        Product result = productService.getProductDetails(product.getId());

        //then
        assertEquals(product, result);
    }


}
