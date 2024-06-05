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
import jakarta.transaction.Transactional;
import net.bytebuddy.utility.dispatcher.JavaDispatcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.junit.jupiter.Container;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest
class ProductRepositoryTest {

    @Container
    static MySQLContainer mySQLContainer = new MySQLContainer<>(DockerImageName.parse("mysql:8.0-debian"));

    @DynamicPropertySource
    static void kafkaProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> mySQLContainer.getJdbcUrl());
        registry.add("spring.datasource.driverClassName", () -> mySQLContainer.getDriverClassName());
        registry.add("spring.datasource.username", () -> mySQLContainer.getUsername());
        registry.add("spring.datasource.password", () -> mySQLContainer.getPassword());
        registry.add("spring.jpa.hibernate.ddl-auto",() -> "update");
    }


    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrdersRepository ordersRepository;

    @Autowired
    private OrderProductRepository orderProductRepository;

    private User user1;
    private Category pCategory;
    private Category cCategory;
    private Category cCategory2;
    private Product product1;
    private Product product2;
    private Product product3;

    @BeforeEach
    public void setUp(){

        user1 = new User("uid1", "password1", null, null);
        userRepository.save(user1);

        pCategory = new Category("소설", null);
        cCategory = new Category("로맨스", pCategory);
        cCategory2 = new Category("SF", pCategory);
        categoryRepository.save(pCategory);
        categoryRepository.save(cCategory);
        categoryRepository.save(cCategory2);

        product1 = new Product("product1", "image1", 10000, 11, cCategory, "writer1", "publisher1");
        product2 = new Product("product2", "image2", 20000, 12, cCategory, "writer2", "publisher2");
        product3 = new Product("book1", "image2", 30000, 12, cCategory2, "writer2", "publisher2");
        productRepository.save(product1);
        productRepository.save(product2);
        productRepository.save(product3);

    }

    // 인기순 -> product, orders, order_product 테이블에 넣은 data 생성해야함
    // 가격순 -> product 테이블에 넣을 data 생성해야함
    @Test
    @DisplayName("쿼리 검색 & 인기순 정렬")
    @Transactional
    void querySearchAndSortByPopularity(){
        //given
        Orders orders1 = new Orders("name1", "address1", "123-456-7890", LocalDateTime.now(), true, 10000, user1);
        Orders orders2 = new Orders("name2", "address2", "123-456-7892", LocalDateTime.now(), true, 20000, user1);
        Orders orders3 = new Orders("name3", "address3", "123-456-7892", LocalDateTime.now().minusMonths(2), true, 20000, user1);
        ordersRepository.save(orders1);
        ordersRepository.save(orders2);
        ordersRepository.save(orders3);

        // 고려사항: 한달이내 총 판매량
        OrderProduct orderProduct1 = new OrderProduct(1, true, orders1, product1);
        OrderProduct orderProduct2 = new OrderProduct(1, true, orders1, product2);
        OrderProduct orderProduct3 = new OrderProduct(1, true, orders2, product1);
        OrderProduct orderProduct4 = new OrderProduct(3, true, orders2, product2);
        OrderProduct orderProduct5 = new OrderProduct(3, true, orders2, product3); // 쿼리 검색 제외
        OrderProduct orderProduct6 = new OrderProduct(4, true, orders3, product1); // 두달 전 주문 검색 제외
        orderProductRepository.save(orderProduct1);
        orderProductRepository.save(orderProduct2);
        orderProductRepository.save(orderProduct3);
        orderProductRepository.save(orderProduct4);
        orderProductRepository.save(orderProduct5);
        orderProductRepository.save(orderProduct6);


        Pageable pageable = PageRequest.of(0, 10);


        //when
        List<ProductListDto> productList = productRepository.searchProductsInQuery("product", "popularity", pageable).getContent();

        //then
        assertThat(productList.size()).isEqualTo(2);
        assertThat(productList.get(0).getName()).isEqualTo("product2");
        assertThat(productList.get(1).getName()).isEqualTo("product1");
    }

    @Test
    @DisplayName("카테고리 & 인기순 정렬")
    @Transactional
    void categorySearchAndSortByPopularity(){
        //given
        Orders orders1 = new Orders("name1", "address1", "123-456-7890", LocalDateTime.now(), true, 10000, user1);
        Orders orders2 = new Orders("name2", "address2", "123-456-7892", LocalDateTime.now(), true, 20000, user1);
        Orders orders3 = new Orders("name2", "address2", "123-456-7892", LocalDateTime.now().minusMonths(2), true, 20000, user1);
        ordersRepository.save(orders1);
        ordersRepository.save(orders2);
        ordersRepository.save(orders3);

        OrderProduct orderProduct1 = new OrderProduct(1, true, orders1, product1);
        OrderProduct orderProduct2 = new OrderProduct(1, true, orders1, product2);
        OrderProduct orderProduct3 = new OrderProduct(1, true, orders2, product1);
        OrderProduct orderProduct4 = new OrderProduct(3, true, orders2, product2);
        OrderProduct orderProduct5 = new OrderProduct(3, true, orders2, product3); // 카테고리 검색 제외
        OrderProduct orderProduct6 = new OrderProduct(4, true, orders3, product1); // 두달 전 주문 검색 제외
        orderProductRepository.save(orderProduct1);
        orderProductRepository.save(orderProduct2);
        orderProductRepository.save(orderProduct3);
        orderProductRepository.save(orderProduct4);
        orderProductRepository.save(orderProduct5);
        orderProductRepository.save(orderProduct6);

        Pageable pageable = PageRequest.of(0, 10);


        //when
        List<ProductListDto> productList = productRepository.searchProductsInCategory(pageable, cCategory.getId(), "popularity").getContent();

        //then
        assertThat(productList.size()).isEqualTo(2);
        assertThat(productList.get(0).getName()).isEqualTo("product2");
        assertThat(productList.get(1).getName()).isEqualTo("product1");

    }


    @Test
    @DisplayName("쿼리 검색 & 낮은 가격순 정렬")
    @Transactional
    void querySearchAndSortByPrice(){
        //given
        Pageable pageable = PageRequest.of(0, 10);

        //when
        List<ProductListDto> productList = productRepository.searchProductsInQuery("product", "price", pageable).getContent();

        //then
        assertThat(productList.size()).isEqualTo(2);
        assertThat(productList.get(0).getName()).isEqualTo("product1");
        assertThat(productList.get(1).getName()).isEqualTo("product2");
    }


    @Test
    @DisplayName("카테고리 검색 & 낮은 가격순 정렬")
    @Transactional
    void categorySearchAndSortByPrice(){
        //given
        Pageable pageable = PageRequest.of(0, 10);

        //when
        List<ProductListDto> productList = productRepository.searchProductsInCategory(pageable, cCategory.getId(), "price").getContent();

        //then
        assertThat(productList.size()).isEqualTo(2);
        assertThat(productList.get(0).getName()).isEqualTo("product1");
        assertThat(productList.get(1).getName()).isEqualTo("product2");
    }
}

