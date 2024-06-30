package com.shopping.book.cart.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopping.book.cart.dto.SaveCartDto;
import com.shopping.book.cart.dto.UpdateCartDto;
import com.shopping.book.cart.service.CartService;
import com.shopping.book.user.entity.User;
import com.shopping.book.user.service.PrincipalDetails;
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Map;

import static com.shopping.book.user.entity.QUser.user;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(CartRestController.class)
@AutoConfigureMockMvc
class CartRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CartService cartService; // MockBean으로 주입

    private PrincipalDetails principalDetails;

    @BeforeEach
    public void setUp(){
        User user = User.builder().id(1L).role("ROLE_USER").build();
        principalDetails = new PrincipalDetails(user, null);
    }

    @Test
    @DisplayName("장바구니 저장 테스트")
    public void testSaveCart() throws Exception {
        SaveCartDto cartDto = new SaveCartDto(5, 1L, 1L);

        doNothing().when(cartService).saveCart(cartDto);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/cart")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cartDto))
                        .with(csrf())
                        .with(SecurityMockMvcRequestPostProcessors.user(principalDetails))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("성공"));
    }

    @Test
    @DisplayName("장바구니 업데이트 테스트")
    public void testUpdateCart() throws Exception {
        UpdateCartDto updateCartDto = new UpdateCartDto(1L, 1L, 5);

        mockMvc.perform(MockMvcRequestBuilders.patch("/api/cart")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateCartDto))
                        .with(csrf())
                        .with(SecurityMockMvcRequestPostProcessors.user(principalDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("성공"));
    }

    @Test
    @DisplayName("장바구니 삭제 테스트")
    public void testDeleteCart() throws Exception {
        Long cartItemId = 1L;


        mockMvc.perform(MockMvcRequestBuilders.delete("/api/cart")
                        .param("cartItemId", String.valueOf(cartItemId))
                        .with(csrf())
                        .with(SecurityMockMvcRequestPostProcessors.user(principalDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("성공"));
    }

    @Test
    @DisplayName("장바구니 저장 실패 시 예외 처리 테스트")
    public void testSaveCartExceptionHandler() throws Exception {
        SaveCartDto cartDto = new SaveCartDto(5, 1L, 1L);

        // 예외를 발생시키도록 설정
        doThrow(new RuntimeException("장바구니 저장에 실패하였습니다.")).when(cartService).saveCart(any(SaveCartDto.class));

        mockMvc.perform(MockMvcRequestBuilders.post("/api/cart")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cartDto))
                        .with(csrf())
                        .with(SecurityMockMvcRequestPostProcessors.user(principalDetails)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("장바구니 저장에 실패하였습니다."));
    }
}