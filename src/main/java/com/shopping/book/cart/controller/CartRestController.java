package com.shopping.book.cart.controller;

import com.shopping.book.cart.dto.SaveCartDto;
import com.shopping.book.cart.dto.UpdateCartDto;
import com.shopping.book.cart.service.CartService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/api/cart")
public class CartRestController {

    private CartService cartService;

    //장바구니 저장
    @PostMapping
    public ResponseEntity saveCart(@RequestBody SaveCartDto cartDto) {
        cartService.saveCart(cartDto);
        return ResponseEntity.status(HttpStatus.OK).body(Map.of("message", "성공"));
    }


    @PatchMapping
    public ResponseEntity updateCart(@RequestBody UpdateCartDto updateCartDto) {
        cartService.updateCart(updateCartDto);
        return ResponseEntity.status(HttpStatus.OK).body(Map.of("message", "성공"));
    }

    //장바구니 삭제
    @DeleteMapping
    public ResponseEntity deleteCart(@RequestParam Long cartItemId) {
        cartService.deleteCart(cartItemId);
        return ResponseEntity.ok(Map.of("message", "성공"));

    }

}
