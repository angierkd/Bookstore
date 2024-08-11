package com.shopping.book.product.controller;

import com.shopping.book.order.controller.OrderRestController;
import com.shopping.book.order.service.OrderService;
import com.shopping.book.product.dto.ProductListDto;
import com.shopping.book.product.entity.Product;
import com.shopping.book.product.service.ProductService;
import com.shopping.book.user.config.SecurityConfig;
import com.shopping.book.user.entity.User;
import com.shopping.book.user.service.PrincipalDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductViewController.class)
@AutoConfigureMockMvc
@Import(SecurityConfig.class) // 시큐리티 설정 추가
public class ProductViewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productservice;

    @Test
    @DisplayName("메인페이지 테스트")
    public void homepageTest() throws Exception {
        ProductListDto productListDto = new ProductListDto();
        List<ProductListDto> productList = List.of(productListDto);

        Pageable pageable = PageRequest.of(0, 8);
        Page<ProductListDto> page = new PageImpl<>(productList, pageable, productList.size());

        when(productservice.getProductsBySearch("", "popularity", pageable)).thenReturn(page);

        // when & then
        mockMvc.perform(get("/product/homepage"))
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(model().attributeExists("products"))
                .andExpect(model().attribute("products", page));
    }

    @Test
    @DisplayName("카테고리 + 인기순/낮은 가격순으로 상품 검색")
    public void getProductsByCategoryTest() throws Exception {
        //given
        ProductListDto productListDto = new ProductListDto();
        List<ProductListDto> productList = List.of(productListDto);

        Pageable pageable = PageRequest.of(0, 8);
        Page<ProductListDto> page = new PageImpl<>(productList, pageable, productList.size());

        when(productservice.getProductsByCategory(any(Pageable.class), anyLong(), anyString())).thenReturn(page);

        //when & then
        mockMvc.perform(get("/product/search/category")
                        .param("category", String.valueOf(1L))
                        .param("sort", "popularity"))
                .andExpect(status().isOk())
                .andExpect(view().name("product-list"))
                .andExpect(model().attributeExists("products"))
                .andExpect(model().attributeExists("category"))
                .andExpect(model().attributeExists("sort"))
                .andExpect(model().attributeExists("currentPath"))
                .andExpect(model().attribute("products", page))
                .andExpect(model().attribute("category", 1L))
                .andExpect(model().attribute("sort", "popularity"));
    }

    @Test
    @DisplayName("검색어 + 인기순/낮은 가격순으로 상품 검색")
    public void getProductsBySearchTest() throws Exception {
        //given
        ProductListDto productListDto = new ProductListDto();
        List<ProductListDto> productList = List.of(productListDto);

        Pageable pageable = PageRequest.of(0, 8);
        Page<ProductListDto> page = new PageImpl<>(productList, pageable, productList.size());

        when(productservice.getProductsBySearch(anyString(), anyString(), any(Pageable.class))).thenReturn(page);

        //when & then
        mockMvc.perform(get("/product/search/query")
                        .param("query", "소설")
                        .param("sort", "popularity"))
                .andExpect(status().isOk())
                .andExpect(view().name("product-list"))
                .andExpect(model().attributeExists("products"))
                .andExpect(model().attributeExists("query"))
                .andExpect(model().attributeExists("sort"))
                .andExpect(model().attributeExists("currentPath"))
                .andExpect(model().attribute("products", page))
                .andExpect(model().attribute("query", "소설"))
                .andExpect(model().attribute("sort", "popularity"));
    }

    @Test
    @DisplayName("상품 상세 페이지 테스트")
    public void getProductDetailsTest() throws Exception {
        // given
        Long productId = 1L;
        Product product = Product.builder()
                .id(productId)
                .name("Sample Product")
                .image("/images/sample.jpg")
                .price(1000)
                .stock(10)
                .writer("Author")
                .publisher("Publisher")
                .category(null) // Assuming category is not needed for this test
                .build();

        when(productservice.getProductDetails(productId)).thenReturn(product);

        // When & Then
        mockMvc.perform(get("/product/{id}", productId)
                        .contentType(MediaType.TEXT_HTML))
                .andDo(MockMvcResultHandlers.print()) // 요청과 응답 내용을 콘솔에 출력
                .andExpect(status().isOk())
                .andExpect(view().name("product"))
                .andExpect(model().attributeExists("product"))
                .andExpect(model().attribute("product", product));
    }
}
