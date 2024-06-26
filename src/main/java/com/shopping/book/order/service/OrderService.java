package com.shopping.book.order.service;

import com.shopping.book.cart.entity.Cart;
import com.shopping.book.cart.repository.CartRepository;
import com.shopping.book.order.entity.Orders;
import com.shopping.book.order.repository.OrdersRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class OrderService {

    private final OrdersRepository orderRepository;
    private final CartRepository cartRepository;

    // 주문서
    public List<Cart> getBill(Long userId) {
        List<Cart> cartList = cartRepository.findByUserId(userId);
        return cartList;
    }

    public List<Orders> getOrderList(Long id) {
        List<Orders> orders = orderRepository.findAllByUserIdAndStatus(id);
        return orders;
    }
}
