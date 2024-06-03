package com.shopping.book.order.repository;

import com.shopping.book.cart.entity.Cart;
import com.shopping.book.order.entity.OrderCancel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderCancelRepository extends JpaRepository<OrderCancel, Long> {
}
