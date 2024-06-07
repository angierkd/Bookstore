package com.shopping.book.cart.service;

import com.shopping.book.cart.dto.SaveCartDto;
import com.shopping.book.cart.dto.UpdateCartDto;
import com.shopping.book.cart.entity.Cart;
import com.shopping.book.cart.repository.CartRepository;
import com.shopping.book.product.entity.Product;
import com.shopping.book.product.repository.ProductRepository;
import com.shopping.book.user.entity.User;
import com.shopping.book.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@AllArgsConstructor
@Service
public class CartService {

    private CartRepository cartRepository;

    private UserRepository userRepository;

    private ProductRepository productRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public void saveCart(SaveCartDto cartDto) {
        // 재고 체크
        checkStock(cartDto.getProductId(), cartDto.getQuantity());

        User user = userRepository.findById(cartDto.getUserId())
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));
        Product product = productRepository.findById(cartDto.getProductId())
                .orElseThrow(() -> new RuntimeException("제품을 찾을 수 없습니다."));

        Cart existingCart = cartRepository.findByUserIdAndProductId(user.getId(), product.getId());

        if (existingCart != null) { //같은 제품이 있다면 update
            existingCart.setQuantity(existingCart.getQuantity() + cartDto.getQuantity());
        }else {
            //같은 제품이 없다면 만들기
            Cart cart = SaveCartDto.convertToEntity(cartDto.getQuantity(), user, product);
            cartRepository.save(cart);
        }
    }


    public List<Cart> getCart(Long userId) {
        List<Cart> userCarts = cartRepository.findByUserId(userId);
        return userCarts;
    }

    @Transactional
    public void updateCart(UpdateCartDto updateCartDto) {
        // 재고 체크
        checkStock(updateCartDto.getProductId(), updateCartDto.getQuantity());

        Cart cartItem = cartRepository.findById(updateCartDto.getCartItemId())
                .orElseThrow(() -> new EntityNotFoundException("카트를 찾을 수 없습니다."));

        if (updateCartDto.getQuantity() <= 0) {
            deleteCart(updateCartDto.getCartItemId()); // 수량이 0인 경우 삭제
        } else {
            cartItem.setQuantity(updateCartDto.getQuantity());
        }
    }


    public void deleteCart(Long cartItemId) {
        cartRepository.deleteById(cartItemId);
    }


    public void checkStock(Long productId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("제품을 찾을 수 없습니다."));

        if (product.getStock() < quantity) {
            throw new RuntimeException("재고가 부족합니다.");
        }
    }
}
