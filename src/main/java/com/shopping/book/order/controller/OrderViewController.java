package com.shopping.book.order.controller;

import com.shopping.book.cart.entity.Cart;
import com.shopping.book.order.entity.OrderProduct;
import com.shopping.book.order.entity.Orders;
import com.shopping.book.order.service.OrderService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@AllArgsConstructor
@Controller
@RequestMapping("/order")
public class OrderViewController {

    private OrderService orderService;

    //주문서 조회
    @GetMapping
    public String getOrder(Model model) {
        List<Cart> userCarts = orderService.getBill(12L); //사용자 아이디
        int totalPrice = userCarts.stream().mapToInt(cartItem -> cartItem.getProduct().getPrice() * cartItem.getQuantity()).sum();
        model.addAttribute("userCarts", userCarts);
        model.addAttribute("totalPrice", totalPrice);
        return "orders";
    }

    //결제목록 조회
    @GetMapping("/list")
    public String getOrderList(Model model){
        List<Orders> orders = orderService.getOrderList(12L); //사용자 아이디
        model.addAttribute("orders", orders);
        return "order-list";
    }

    //결제 성공 페이지
    @GetMapping("/success")
    public String orderSuccess(){
        return "payment-success";
    }

    //결제 실패 페이지
    @GetMapping("/fail")
    public String orderFail(){
        return "payment-fail";
    }

}