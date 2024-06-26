package com.shopping.book.order.repository;

import com.shopping.book.cart.entity.Cart;
import com.shopping.book.order.entity.Orders;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrdersRepository extends JpaRepository<Orders, Long> {

    @Query("SELECT o FROM Orders o JOIN FETCH o.orderProducts WHERE o.user.id = :userId AND o.status = true")
    List<Orders> findAllByUserIdAndStatus(@Param("userId") Long userId);
}
