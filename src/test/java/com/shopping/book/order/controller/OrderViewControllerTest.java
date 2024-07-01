package com.shopping.book.order.controller;

import com.shopping.book.cart.entity.Cart;
import com.shopping.book.order.entity.Orders;
import com.shopping.book.order.service.OrderService;
import com.shopping.book.product.entity.Product;
import com.shopping.book.user.entity.User;
import com.shopping.book.user.service.PrincipalDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderViewController.class)
@AutoConfigureMockMvc
class OrderViewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    private PrincipalDetails principalDetails;

    @BeforeEach
    public void setUp(){
        User user = User.builder().id(1L).role("ROLE_USER").build();
        principalDetails = new PrincipalDetails(user, null);
    }

    @Test
    @DisplayName("주문서 조회 테스트")
    public void testGetOrder() throws Exception {

        //given
        Product product = Product.builder().id(1L).name("Product1").price(10000).build();
        Cart cart = Cart.builder().id(1L).quantity(2).product(product).build();
        List<Cart> userCarts = Arrays.asList(cart);

        when(orderService.getBill(anyLong())).thenReturn(userCarts);

        // when & then
        mockMvc.perform(get("/order")
                        .with(csrf())
                        .with(SecurityMockMvcRequestPostProcessors.user(principalDetails)))
                .andExpect(status().isOk())
                .andExpect(view().name("orders"))
                .andExpect(model().attributeExists("userCarts"))
                .andExpect(model().attributeExists("totalPrice"))
                .andExpect(model().attribute("userCarts", userCarts))
                .andExpect(model().attribute("totalPrice", 20000));
    }

    @Test
    @DisplayName("주문 목록 조회 테스트")
    void testGetOrderList() throws Exception {
        //given
        Orders order = Orders.builder().id(1L).build();
        List<Orders> orders = Arrays.asList(order);
        when(orderService.getOrderList(anyLong())).thenReturn(orders);

        //when & then
        mockMvc.perform(get("/order/list")
                        .with(csrf())
                        .with(SecurityMockMvcRequestPostProcessors.user(principalDetails)))
                .andExpect(status().isOk())
                .andExpect(view().name("order-list"))
                .andExpect(model().attributeExists("orders"))
                .andExpect(model().attribute("orders", orders));
    }

    @Test
    @DisplayName("결제 성공 페이지 테스트")
    void orderSuccessTest() throws Exception {
        mockMvc.perform(get("/order/success")
                        .with(csrf())
                        .with(SecurityMockMvcRequestPostProcessors.user(principalDetails)))
                .andExpect(status().isOk())
                .andExpect(view().name("payment-success"));
    }

    @Test
    @DisplayName("결제 실패 페이지 테스트")
    void orderFailTest() throws Exception {
        mockMvc.perform(get("/order/fail")
                        .with(csrf())
                        .with(SecurityMockMvcRequestPostProcessors.user(principalDetails)))
                .andExpect(status().isOk())
                .andExpect(view().name("payment-fail"));
    }

}