package com.shopping.book.cart.controller;

import com.shopping.book.cart.entity.Cart;
import com.shopping.book.cart.service.CartService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Slf4j
@AllArgsConstructor
@Controller
@RequestMapping("/cart")
public class CartViewController {

    private final CartService cartService;

    //장바구니 가져오기 - 품절 상품 품절 표시
    @GetMapping
    public String getCart(Model model){
        List<Cart> userCarts = cartService.getCart(12L);
        model.addAttribute("userCarts", userCarts);
        return "cart";
    }
}
