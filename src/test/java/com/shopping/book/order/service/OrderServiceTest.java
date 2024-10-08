package com.shopping.book.order.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopping.book.cart.entity.Cart;
import com.shopping.book.cart.repository.CartRepository;
import com.shopping.book.order.dto.OrderCompleteDto;
import com.shopping.book.order.dto.OrderCreateDto;
import com.shopping.book.order.entity.OrderCancel;
import com.shopping.book.order.entity.OrderProduct;
import com.shopping.book.order.entity.Orders;
import com.shopping.book.order.repository.OrderCancelRepository;
import com.shopping.book.order.repository.OrderProductRepository;
import com.shopping.book.order.repository.OrdersRepository;
import com.shopping.book.product.entity.Product;
import com.shopping.book.product.repository.ProductRepository;
import com.shopping.book.user.entity.User;
import com.siot.IamportRestClient.IamportClient;
import com.siot.IamportRestClient.exception.IamportResponseException;
import com.siot.IamportRestClient.response.IamportResponse;
import com.siot.IamportRestClient.response.Payment;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.*;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;


@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OrderServiceTest {

    @Mock
    private OrderProductRepository orderProductRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OrdersRepository orderRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private OrderCancelRepository orderCancelRepository;

    @Mock
    private IamportClient iamportClient; // 아임포트 API 클라이언트

    @InjectMocks
    private OrderService orderService;

    @Test
    @DisplayName("주문 생성 테스트")
    public void testCreateOrder() {
        //given
        Product product = Product.builder()
                .id(1L)
                .name("Test Product")
                .price(10000)
                .stock(100)
                .build();

        User user = User.builder().id(1L).build();

        Cart cart = Cart.builder()
                .id(1L)
                .quantity(2)
                .user(user)
                .product(product)
                .build();

        Orders order = Orders.builder().build();

        OrderProduct orderProduct = OrderProduct.builder()
                .orders(order)
                .product(product)
                .quantity(1)
                .build();

        List<OrderProduct> orderProducts = Arrays.asList(orderProduct);
        List<Long> cartIds = List.of(cart.getId());
        List<Cart> carts = List.of(cart);

        OrderCreateDto orderCreateDto = new OrderCreateDto("hi", "송파구", "010-0000-0000", cartIds);

        // When
        when(cartRepository.findAllById(anyList())).thenReturn(carts);
        when(orderRepository.save(any(Orders.class))).thenReturn(order);
        when(orderProductRepository.saveAll(anyList())).thenReturn(orderProducts);

        Orders createdOrder = orderService.createOrder(orderCreateDto);

        // Then
        assertNotNull(createdOrder);
        assertEquals("hi", createdOrder.getName());
        assertEquals("송파구", createdOrder.getAddress());
        assertEquals("010-0000-0000", createdOrder.getPhoneNum());
        assertEquals(20000, createdOrder.getTotalPrice());
        assertEquals(user, createdOrder.getUser());

        verify(cartRepository, times(1)).findAllById(anyList());
        verify(orderRepository, times(1)).save(any(Orders.class));
        verify(orderProductRepository, times(1)).saveAll(anyList());

    }


    @Test
    @DisplayName("결제 완료 후 테스트")
    public void testCompleteOrder() throws IamportResponseException, IOException {
        //given
        Product product = Product.builder()
                .id(1L)
                .name("Test Product")
                .price(10000)
                .stock(100)
                .build();

        User user = User.builder().id(1L).build();

        Orders order = Orders.builder()
                .user(user)
                .status(false)
                .build();

        OrderProduct orderProduct = OrderProduct.builder()
                .orders(order)
                .product(product)
                .quantity(2)
                .build();

        List<OrderProduct> orderProducts = List.of(orderProduct);
        OrderCompleteDto orderCompleteDto = new OrderCompleteDto("imp-123456", order);

        Payment payment = mock(Payment.class);
        when(payment.getStatus()).thenReturn("paid");

        IamportResponse<Payment> paymentResponse = mock(IamportResponse.class);
        when(paymentResponse.getResponse()).thenReturn(payment);

        when(iamportClient.paymentByImpUid(orderCompleteDto.getImpUid())).thenReturn(paymentResponse);
        when(orderProductRepository.findAllByOrders(order)).thenReturn(orderProducts);
        when(productRepository.findByIdWithPessimisticLock(orderProduct.getProduct().getId())).thenReturn(Optional.ofNullable(product));
        when(productRepository.save(product)).thenReturn(product);
        doNothing().when(cartRepository).deleteByProductIdAndUserId(product.getId(), user.getId());

        //when
        orderService.completeOrder(orderCompleteDto);

        //then
        assertEquals(true, order.getStatus());
        assertEquals(98, product.getStock());

        verify(iamportClient, times(1)).paymentByImpUid(anyString());
        verify(orderProductRepository, times(1)).findAllByOrders(any(Orders.class));
        verify(productRepository, times(1)).findByIdWithPessimisticLock(anyLong());
        verify(productRepository, times(1)).save(any(Product.class));
        verify(cartRepository, times(1)).deleteByProductIdAndUserId(anyLong(), anyLong());
    }

    @Test
    @DisplayName("결제 완료 - paid가 아닐 경우")
    public void testCompleteOrder_Fail() throws IamportResponseException, IOException {
        //given
        Product product = Product.builder()
                .id(1L)
                .name("Test Product")
                .price(10000)
                .stock(100)
                .build();

        User user = User.builder().id(1L).build();

        Orders order = Orders.builder()
                .user(user)
                .status(false)
                .build();

        OrderProduct orderProduct = OrderProduct.builder()
                .orders(order)
                .product(product)
                .quantity(2)
                .build();

        List<OrderProduct> orderProducts = List.of(orderProduct);
        OrderCompleteDto orderCompleteDto = new OrderCompleteDto("imp-123456", order);

        Payment payment = mock(Payment.class);
        when(payment.getStatus()).thenReturn("failed");

        IamportResponse<Payment> paymentResponse = mock(IamportResponse.class);
        when(paymentResponse.getResponse()).thenReturn(payment);

        when(iamportClient.paymentByImpUid(orderCompleteDto.getImpUid())).thenReturn(paymentResponse);
        when(orderProductRepository.findAllByOrders(order)).thenReturn(orderProducts);
        when(productRepository.findByIdWithPessimisticLock(orderProduct.getProduct().getId())).thenReturn(Optional.ofNullable(product));
        when(productRepository.save(product)).thenReturn(product);
        doNothing().when(cartRepository).deleteByProductIdAndUserId(product.getId(), user.getId());

        //when & then
        RuntimeException thrown = assertThrows(
                RuntimeException.class,
                () -> orderService.completeOrder(orderCompleteDto),
                "Expected completeOrder() to throw, but it didn't"
        );

        assertTrue(thrown.getMessage().contains("Payment not completed"));
        verify(orderRepository, never()).save(any(Orders.class));
        verify(orderProductRepository, never()).findAllByOrders(any(Orders.class));
        verify(cartRepository, never()).deleteByProductIdAndUserId(anyLong(), anyLong());
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("주문 취소 테스트")
    public void testCancelOrderProduct() {
        //given
        Product product = Product.builder()
                .id(1L)
                .name("Test Product")
                .price(10000)
                .stock(100)
                .build();

        User user = User.builder().id(1L).build();

        Orders order = Orders.builder()
                .status(true)
                .build();

        OrderProduct orderProduct = OrderProduct.builder()
                .id(1L)
                .orders(order)
                .status(false)
                .product(product)
                .quantity(2)
                .build();

        OrderCancel orderCancel = OrderCancel.builder()
                .ordersProduct(orderProduct)
                .build();

        when(orderProductRepository.findById(orderProduct.getId())).thenReturn(Optional.of(orderProduct));
        when(orderProductRepository.save(orderProduct)).thenReturn(orderProduct);
        when(orderCancelRepository.save(orderCancel)).thenReturn(orderCancel);

        //when
        orderService.cancelOrderProduct(orderProduct.getId());

        //then
        assertEquals(true, orderProduct.getStatus());

        verify(orderProductRepository, times(1)).findById(anyLong());
        verify(orderProductRepository, times(1)).save(any(OrderProduct.class));
        verify(orderCancelRepository, times(1)).save(any(OrderCancel.class));

    }

    @Test
    @DisplayName("주문 취소 테스트 - 상품 없음")
    public void testCancelOrderProduct_NotFound() {
        // given
        Long orderProductId = 1L;

        when(orderProductRepository.findById(orderProductId)).thenReturn(java.util.Optional.empty());

        // when & then
        EntityNotFoundException thrown = assertThrows(
                EntityNotFoundException.class,
                () -> orderService.cancelOrderProduct(orderProductId),
                "Expected cancelOrderProduct() to throw, but it didn't"
        );

        assertTrue(thrown.getMessage().contains("Order not found with id " + orderProductId));
        verify(orderProductRepository, never()).save(any(OrderProduct.class));
        verify(orderCancelRepository, never()).save(any(OrderCancel.class));
    }

    @Test
    @DisplayName("주문서 조회 테스트")
    public void getBillTest() {
        //given
        Cart cart = Cart.builder().build();
        List<Cart> cartList = List.of(cart);
        long userId = 1L;

        when(cartRepository.findByUserId(userId)).thenReturn(cartList);

        //when
        List<Cart> result = orderService.getBill(userId);

        //when & then
        assertEquals(cartList, result);
        verify(cartRepository, times(1)).findByUserId(userId);
    }

    @Test
    @DisplayName("주문 목록 조회 테스트")
    public void getOrderListTest() {
        //given
        Orders order = Orders.builder().build();
        List<Orders> orderList = List.of(order);
        long userId = 1L;

        when(orderRepository.findAllByUserIdAndStatus(userId)).thenReturn(orderList);

        //when
        List<Orders> result = orderService.getOrderList(userId);

        //then
        assertEquals(orderList, result);
        verify(orderRepository, times(1)).findAllByUserIdAndStatus(userId);
    }
}