package com.shopping.book.order.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopping.book.cart.controller.CartRestController;
import com.shopping.book.cart.dto.SaveCartDto;
import com.shopping.book.cart.service.CartService;
import com.shopping.book.order.dto.CancelOrderRequestDto;
import com.shopping.book.order.dto.OrderCompleteDto;
import com.shopping.book.order.dto.OrderCreateDto;
import com.shopping.book.order.entity.OrderProduct;
import com.shopping.book.order.entity.Orders;
import com.shopping.book.order.service.OrderService;
import com.shopping.book.user.entity.User;
import com.shopping.book.user.service.PrincipalDetails;
import com.siot.IamportRestClient.exception.IamportResponseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(OrderRestController.class)
@AutoConfigureMockMvc
class OrderRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderService orderService; // MockBean으로 주입

    private PrincipalDetails principalDetails;

    @BeforeEach
    public void setUp(){
        User user = User.builder().id(1L).role("ROLE_USER").build();
        principalDetails = new PrincipalDetails(user, null);
    }

    @Test
    @DisplayName("주문서 생성 테스트")
    public void testCreateOrder() throws Exception {

        //given
        OrderCreateDto orderCreateDto = new OrderCreateDto("mj", "서울", "010-0000-0000", List.of(1L));

        Orders order = Orders.builder()
                .id(1L).status(false).build();


        when(orderService.createOrder(orderCreateDto)).thenReturn(order);

        //when & then
        mockMvc.perform(post("/api/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderCreateDto))
                        .with(csrf())
                        .with(SecurityMockMvcRequestPostProcessors.user(principalDetails)))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(jsonPath("$.id").value(order.getId()))
                .andExpect(jsonPath("$.status").value(order.getStatus()));
    }

    @Test
    @DisplayName("결제완료 후 테스트 - 성공")
    public void testOrderComplete_Success() throws Exception {
        //given
        OrderCompleteDto orderCompleteDto = new OrderCompleteDto("imp_123456", new Orders());

        doNothing().when(orderService).completeOrder(orderCompleteDto);

        //when & then
        mockMvc.perform(post("/api/order/complete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderCompleteDto))
                        .with(csrf())
                        .with(SecurityMockMvcRequestPostProcessors.user(principalDetails)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(jsonPath("$.message").value("결제 성공"));

    }

    @Test
    @DisplayName("결제 취소 테스트")
    public void testOrderCancel() throws Exception {
        //given
        CancelOrderRequestDto request = new CancelOrderRequestDto(1L, 1L, 2, "그냥");

        Map<String, Object> cancelResponse = new HashMap<>();
        cancelResponse.put("message", "취소 성공");
        when(orderService.cancelPayment(request.getOrderId(), request.getAmount(), request.getReason())).thenReturn(cancelResponse);
        doNothing().when(orderService).cancelOrderProduct(request.getOrderProductId());

        //when & then
        mockMvc.perform(post("/api/order/cancel")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                        .with(csrf())
                        .with(SecurityMockMvcRequestPostProcessors.user(principalDetails)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(jsonPath("$.message").value("취소 성공"));
    }

}