package com.shopping.book.order.repository;

import com.shopping.book.cart.entity.Cart;
import com.shopping.book.order.entity.OrderProduct;
import com.shopping.book.order.entity.Orders;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface OrderProductRepository extends JpaRepository<OrderProduct, Long> {
    List<OrderProduct> findAllByOrders(Orders order);

    void deleteByOrders(Orders order);
}
