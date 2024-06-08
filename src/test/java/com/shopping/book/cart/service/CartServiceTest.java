package com.shopping.book.cart.service;

import com.shopping.book.cart.dto.SaveCartDto;
import com.shopping.book.cart.dto.UpdateCartDto;
import com.shopping.book.cart.entity.Cart;
import com.shopping.book.cart.repository.CartRepository;
import com.shopping.book.product.entity.Product;
import com.shopping.book.product.repository.ProductRepository;
import com.shopping.book.user.entity.User;
import com.shopping.book.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

class CartServiceTest {
    @Mock
    private CartRepository cartRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private CartService cartService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    //saveCart
    @Test
    @DisplayName("충분한 재고가 있는 경우 장바구니 추가")
    @Transactional
    public void testSaveCartWithSufficientStock() {
        //given
        SaveCartDto cartDto = SaveCartDto.builder()
                .userId(1L)
                .productId(1L)
                .quantity(10)
                .build();

        Product product = Product.builder()  // 충분한 재고
                .stock(10)
                .build();

        User user = new User();

        //when
        when(userRepository.findById(cartDto.getUserId())).thenReturn(java.util.Optional.of(user));
        when(productRepository.findById(cartDto.getProductId())).thenReturn(java.util.Optional.of(product));

        cartService.saveCart(cartDto);

        //then
        verify(cartRepository, times(1)).save(any(Cart.class));
    }

    @Test
    @DisplayName("존재하지 않는 유저 ID로 장바구니 추가하는 경우")
    @Transactional
    public void testSaveCartWithNonExistingUser() {
        //given
        SaveCartDto cartDto = SaveCartDto.builder()
                .userId(9999L)
                .productId(1L)
                .quantity(10)
                .build();

        //when
        when(userRepository.findById(9999L)).thenReturn(Optional.empty());

        //then
        assertThrows(RuntimeException.class, () -> cartService.saveCart(cartDto));
    }

    @Test
    @DisplayName("같은 상품이 장바구니에 들어있는 경우 장바구니 추가")
    @Transactional
    public void testSaveCartWithExistingCartItem() {
        //given
        SaveCartDto cartDto = SaveCartDto.builder()
                .userId(1L)
                .productId(1L)
                .quantity(5) // 증가할 수량
                .build();

        User user = User.builder()
                .id(1L)
                .build();

        Cart cartItem = Cart.builder()
                .quantity(5) // 장바구니에 담긴 수량
                .build();

        Product product = Product.builder()
                .id(1L)
                .stock(20)
                .build();

        //when
        when(userRepository.findById(cartDto.getUserId())).thenReturn(java.util.Optional.of(user));
        when(productRepository.findById(cartDto.getProductId())).thenReturn(java.util.Optional.of(product));
        when(cartRepository.findByUserIdAndProductId(cartDto.getUserId(), cartDto.getProductId())).thenReturn(cartItem);

        cartService.saveCart(cartDto);

        //then
        verify(cartRepository, never()).save(any(Cart.class)); // 장바구니 아이템이 이미 존재하므로 save 메서드 호출되지 않아야 함
        assertEquals(10, cartItem.getQuantity()); // 장바구니 수량이 올바르게 업데이트되었는지 확인
    }

    //updateCart

    @Test
    @DisplayName("장바구니 상품 수량 증가 테스트 (재고 체크 통과)")
    @Transactional
    public void testUpdateCartQuantityWithSufficientStock(){

        //given
        UpdateCartDto updateCartDto = UpdateCartDto
                .builder()
                .cartItemId(1L)
                .productId(1L)
                .quantity(5) //증가할 수량
                .build();

        Cart cartItem = Cart.builder()
                .quantity(4)
                .build(); // 장바구니에 담긴 수량

        Product product = Product.builder()
                .id(1L)
                .stock(20)
                .build(); //재고 수량 20

        //when
        when(cartRepository.findById(updateCartDto.getCartItemId())).thenReturn(Optional.ofNullable(cartItem));
        when(productRepository.findById(updateCartDto.getProductId())).thenReturn(java.util.Optional.of(product));

        cartService.updateCart(updateCartDto);

        //then
        assertEquals(updateCartDto.getQuantity(), cartItem.getQuantity());
    }

    @Test
    @DisplayName("장바구니 상품 수량이 0이 될 경우 테스트")
    @Transactional
    public void testUpdateCartQuantityToZero(){
        //given
        UpdateCartDto updateCartDto = UpdateCartDto.builder()
                .cartItemId(1L)
                .productId(1L)
                .quantity(0) // 수량 0이 됨
                .build();

        Cart cartItem = Cart.builder()
                .id(1L)
                .quantity(1)
                .build();

        //when
        when(cartRepository.findById(updateCartDto.getCartItemId())).thenReturn(Optional.ofNullable(cartItem));

        cartService.updateCart(updateCartDto);

        //then
        verify(cartRepository, times(1)).deleteById(cartItem.getId());
    }

    @Test
    @DisplayName("장바구니 상품 수량 감소 테스트 (재고 체크 없음)")
    @Transactional
    public void testUpdateCartQuantityWithoutStockCheck(){
        //given
        UpdateCartDto updateCartDto = UpdateCartDto.builder()
                .cartItemId(1L)
                .productId(1L)
                .quantity(4) // 감소할 수량
                .build();

        Cart cartItem = Cart.builder()
                .id(1L)
                .quantity(5) // 장바구니 수량
                .build();

        //when
        when(cartRepository.findById(updateCartDto.getCartItemId())).thenReturn(Optional.ofNullable(cartItem));

        cartService.updateCart(updateCartDto);

        //then
        verify(productRepository, never()).findById(anyLong());
        assertEquals(updateCartDto.getQuantity(), cartItem.getQuantity());
    }


    //재고 체크 함수 - 재고가 없는 경우
    @Test
    @DisplayName("재고 체크 (재고 없음)")
    @Transactional
    public void testCheckStockWithoutStock(){
        //given
        UpdateCartDto updateCartDto = UpdateCartDto.builder()
                .cartItemId(1L)
                .productId(1L)
                .quantity(10)
                .build(); // 수량 10

        Product product = Product.builder()
                .id(1L)
                .stock(5)
                .build(); // 재고 수량 5

        //when
        when(productRepository.findById(updateCartDto.getProductId())).thenReturn(Optional.ofNullable(product));

        //then
        assertThrows(RuntimeException.class, () -> cartService.checkStock(updateCartDto.getProductId(), updateCartDto.getQuantity()));
    }

    @Test
    @DisplayName("장바구니 가져오기")
    public void testGetCart(){
        //given
        User user = User.builder()
                .id(1L)
                .build();

        Cart cart1 = Cart.builder()
                .id(1L)
                .user(user)
                .build();

        Cart cart2 = Cart.builder()
                .id(2L)
                .user(user)
                .build();

        List<Cart> userCarts = Arrays.asList(cart1, cart2);

        //when -
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(cartRepository.findByUserId(user.getId())).thenReturn(userCarts);

        //메소드 호출
        List<Cart> retrievedCarts = cartService.getCart(user.getId());

        //then
        assertEquals(2, retrievedCarts.size());
        assertEquals(1L, retrievedCarts.get(0).getId());
        assertEquals(2L, retrievedCarts.get(1).getId());
    }
}