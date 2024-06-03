package com.shopping.book.product.repository;

import com.shopping.book.order.entity.OrderProduct;
import com.shopping.book.order.entity.Orders;
import com.shopping.book.order.repository.OrderProductRepository;
import com.shopping.book.order.repository.OrdersRepository;
import com.shopping.book.product.dto.ProductListDto;
import com.shopping.book.product.entity.Category;
import com.shopping.book.product.entity.Product;
import com.shopping.book.user.entity.User;
import com.shopping.book.user.repository.UserRepository;
import groovy.util.logging.Slf4j;
import jakarta.persistence.*;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@DataJpaTest
//@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ProductRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrdersRepository ordersRepository;

    @Autowired
    private OrderProductRepository orderProductRepository;

    @BeforeEach
    @Transactional
    public void setUp(){

        User user1 = new User("uid1", "password1", null, null);
        userRepository.save(user1);

        Category pCategory = new Category("소설", null);
        Category cCategory = new Category("로맨스", pCategory);
        categoryRepository.save(pCategory);
        categoryRepository.save(cCategory);


        Product product1 = new Product("product1", "image1", 10000, 11, cCategory, "writer1", "publisher1");
        Product product2 = new Product("product2", "image2", 20000, 12, cCategory, "writer2", "publisher2");
        Product product3= new Product("good1", "image3", 30000, 12, cCategory, "writer3", "publisher3");
        productRepository.save(product1);
        productRepository.save(product2);
        productRepository.save(product3);

        //order 2개 생성 - order1: product1 3개 product2 4개 order2: product1 2개 product3 3개
        Orders orders1 = new Orders("name1", "address1", "123-456-7890", LocalDateTime.now(), true, 10000, user1);
        Orders orders2 = new Orders("name2", "address2", "123-456-7892", LocalDateTime.now(), true, 20000, user1);
        ordersRepository.save(orders1);
        ordersRepository.save(orders2);

        OrderProduct orderProduct1 = new OrderProduct(3, true, orders1, product1);
        OrderProduct orderProduct2 = new OrderProduct(4, true, orders1, product2);
        OrderProduct orderProduct3 = new OrderProduct(2, true, orders2, product1);
        OrderProduct orderProduct4 = new OrderProduct(3, true, orders2, product3);
        orderProductRepository.save(orderProduct1);
        orderProductRepository.save(orderProduct2);
        orderProductRepository.save(orderProduct3);
        orderProductRepository.save(orderProduct4);
    }

    // 인기순 -> product, orders, order_product 테이블에 넣은 data 생성해야함
    // 가격순 -> product 테이블에 넣을 data 생성해야함
//    @Test
//    @DisplayName("쿼리 검색&인기순 정렬")
//    void getProduct(){
//        //given
//        //when
//        List<ProductListDto> productList = productRepository.searchProductsInQuery("product", "popularity");
//        //log.info("{}", productList);
//        //then
//        assertThat(productList.size()).isEqualTo(2);
//        assertThat(productList.get(0).getName()).isEqualTo("product1");
//        assertThat(productList.get(1).getName()).isEqualTo("product2");
//    }
//
//    @Test
//    @DisplayName("카테고리&인기순 정렬")
//    void getProduct3(){
//        //given
//        //when
//        List<ProductListDto> productList = productRepository.searchProductsInCategory(35L, "popularity");
//
//        //then
//        assertThat(productList.size()).isEqualTo(2);
//        assertThat(productList.get(0).getName()).isEqualTo("product1");
//        assertThat(productList.get(1).getName()).isEqualTo("product2");
//    }


}

