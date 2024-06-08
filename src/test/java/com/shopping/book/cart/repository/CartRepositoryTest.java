package com.shopping.book.cart.repository;

import com.shopping.book.cart.entity.Cart;
import com.shopping.book.product.entity.Product;
import com.shopping.book.product.repository.ProductRepository;
import com.shopping.book.user.entity.User;
import com.shopping.book.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest
class CartRepositoryTest {

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
    private CartRepository cartRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Test
    @Transactional
    public void testFindByUserIdAndProductId() {
        // given
        User user = User.builder()
                .id(1L).build();

        Product product = Product.builder()
                .id(1L).build();

        Cart cartItem = Cart.builder()
                .user(user)
                .product(product).build();

        userRepository.save(user);
        productRepository.save(product);
        cartRepository.save(cartItem);

        // when
        Cart cart = cartRepository.findByUserIdAndProductId(user.getId(), product.getId());

        // then
        assertEquals(cartItem, cart); // 일단 비어있을 것으로 예상, 실제 데이터를 넣고 테스트할 때 이 부분 수정
    }
}