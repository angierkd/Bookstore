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
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DataJpaTest
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


        user1 = User.builder().build();
        userRepository.save(user1);

        pCategory = Category.builder().name("소설").build();
        cCategory = Category.builder().name("로맨스").parentCategory(pCategory).build();
        cCategory2 = Category.builder().name("SF").parentCategory(pCategory).build();
        categoryRepository.save(pCategory);
        categoryRepository.save(cCategory);
        categoryRepository.save(cCategory2);

        product1 = Product.builder().name("product1").price(10000).category(cCategory).build();
        product2 = Product.builder().name("product2").price(20000).category(cCategory).build();
        product3 = Product.builder().name("book1").price(30000).category(cCategory2).build();
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
        Orders orders1 = Orders.builder().date(LocalDateTime.now()).status(true).user(user1).build();
        Orders orders2 = Orders.builder().date(LocalDateTime.now()).status(true).user(user1).build();
        Orders orders3 = Orders.builder().date(LocalDateTime.now().minusMonths(2)).status(true).user(user1).build();
        ordersRepository.save(orders1);
        ordersRepository.save(orders2);
        ordersRepository.save(orders3);

        // 고려사항: 한달이내 총 판매량
        OrderProduct orderProduct1 = OrderProduct.builder().quantity(1).status(true).orders(orders1).product(product1).build();
        OrderProduct orderProduct2 = OrderProduct.builder().quantity(1).status(true).orders(orders1).product(product2).build();
        OrderProduct orderProduct3 = OrderProduct.builder().quantity(1).status(true).orders(orders2).product(product1).build();
        OrderProduct orderProduct4 = OrderProduct.builder().quantity(3).status(true).orders(orders2).product(product2).build();
        OrderProduct orderProduct5 = OrderProduct.builder().quantity(3).status(true).orders(orders2).product(product3).build();
        OrderProduct orderProduct6 = OrderProduct.builder().quantity(4).status(true).orders(orders3).product(product1).build();
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
        Orders orders1 = Orders.builder().date(LocalDateTime.now()).status(true).user(user1).build();
        Orders orders2 = Orders.builder().date(LocalDateTime.now()).status(true).user(user1).build();
        Orders orders3 = Orders.builder().date(LocalDateTime.now().minusMonths(2)).status(true).user(user1).build();
        ordersRepository.save(orders1);
        ordersRepository.save(orders2);
        ordersRepository.save(orders3);

        // 고려사항: 한달이내 총 판매량
        OrderProduct orderProduct1 = OrderProduct.builder().quantity(1).status(true).orders(orders1).product(product1).build();
        OrderProduct orderProduct2 = OrderProduct.builder().quantity(1).status(true).orders(orders1).product(product2).build();
        OrderProduct orderProduct3 = OrderProduct.builder().quantity(1).status(true).orders(orders2).product(product1).build();
        OrderProduct orderProduct4 = OrderProduct.builder().quantity(3).status(true).orders(orders2).product(product2).build();
        OrderProduct orderProduct5 = OrderProduct.builder().quantity(3).status(true).orders(orders2).product(product3).build();
        OrderProduct orderProduct6 = OrderProduct.builder().quantity(4).status(true).orders(orders3).product(product1).build();
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


    @Test
    @DisplayName("상품 락 테스트")
    public void testFindByIdWithPessimisticLock() {
        Product product = new Product();
        product.setStock(100);
        productRepository.save(product);

        Optional<Product> foundProduct = productRepository.findByIdWithPessimisticLock(product.getId());
        assertThat(foundProduct).isPresent();
        assertThat(foundProduct.get().getId()).isEqualTo(product.getId());
    }
}

