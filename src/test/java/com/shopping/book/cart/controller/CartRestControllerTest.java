package com.shopping.book.cart.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopping.book.cart.dto.SaveCartDto;
import com.shopping.book.cart.dto.UpdateCartDto;
import com.shopping.book.cart.service.CartService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@WebMvcTest(CartRestController.class)
@AutoConfigureMockMvc
class CartRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CartService cartService; // MockBean으로 주입

    @Test
    @DisplayName("장바구니 저장 테스트")
    public void testSaveCart() throws Exception {
        SaveCartDto cartDto = new SaveCartDto(5, 1L, 1L);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/cart")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cartDto)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("성공"));
    }

    @Test
    @DisplayName("장바구니 업데이트 테스트")
    public void testUpdateCart() throws Exception {
        UpdateCartDto updateCartDto = new UpdateCartDto(1L, 1L, 5);

        mockMvc.perform(MockMvcRequestBuilders.patch("/api/cart")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateCartDto)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("성공"));
    }

    @Test
    @DisplayName("장바구니 삭제 테스트")
    public void testDeleteCart() throws Exception {
        Long cartItemId = 1L;

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/cart")
                        .param("cartItemId", String.valueOf(cartItemId)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("성공"));
    }

    @Test
    @DisplayName("장바구니 저장 실패 시 예외 처리 테스트")
    public void testSaveCartExceptionHandler() throws Exception {
        SaveCartDto cartDto = new SaveCartDto(5, 1L, 1L);

        // 예외를 발생시키도록 설정
        doThrow(new RuntimeException("장바구니 저장에 실패하였습니다.")).when(cartService).saveCart(any(SaveCartDto.class));

        mockMvc.perform(MockMvcRequestBuilders.post("/api/cart")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cartDto)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.error").value("장바구니 저장에 실패하였습니다."));
    }
}