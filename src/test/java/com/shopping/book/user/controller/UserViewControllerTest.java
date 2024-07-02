package com.shopping.book.user.controller;

import com.shopping.book.cart.controller.CartRestController;
import com.shopping.book.product.service.ProductService;
import com.shopping.book.user.config.SecurityConfig;
import com.shopping.book.user.entity.User;
import com.shopping.book.user.service.PrincipalDetails;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserViewController.class)
@AutoConfigureMockMvc
@Import(SecurityConfig.class) // 시큐리티 설정 추가
public class UserViewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private PrincipalDetails principalDetails;

    @BeforeEach
    public void setUp() {
        User user = User.builder().id(1L).role("ROLE_USER").build();
        principalDetails = new PrincipalDetails(user, null);
    }


    @Test
    @DisplayName("로그인 페이지 테스트")
    public void loginTest() throws Exception {
        mockMvc.perform(get("/user/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

    @Test
    @DisplayName("로그아웃 테스트")
    public void logoutTest() throws Exception {
        // 세션이 있는 상태로 로그아웃 요청
        mockMvc.perform(get("/user/logout")
                        .sessionAttr("attribute", "value") // 세션에 임의의 속성을 추가하여 세션이 있는 상태를 만듦
                        .with(csrf())
                        .with(SecurityMockMvcRequestPostProcessors.user(principalDetails)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/product/homepage"))
                .andExpect(request -> {
                    HttpSession session = request.getRequest().getSession(false);
                    assert session == null; // 세션이 무효화되었는지 확인
                });
    }
}
