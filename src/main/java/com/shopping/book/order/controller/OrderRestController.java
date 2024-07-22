package com.shopping.book.order.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.shopping.book.order.dto.CancelOrderRequestDto;
import com.shopping.book.order.dto.OrderCompleteDto;
import com.shopping.book.order.dto.OrderCreateDto;
import com.shopping.book.order.entity.Orders;
import com.shopping.book.order.service.OrderService;
import com.siot.IamportRestClient.exception.IamportResponseException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/api/order")
public class OrderRestController {

    private OrderService orderService;

    //주문서 생성
    @PostMapping
    public ResponseEntity<Orders> createOrder(@RequestBody OrderCreateDto orderCreateDto) {
        log.info("Received payment request: {}", orderCreateDto);
        Orders order = orderService.createOrder(orderCreateDto);
        log.info("Created order: {}", order);
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    //결제완료 후
    @PostMapping("/complete")
    public ResponseEntity orderComplete(@RequestBody OrderCompleteDto orderCompleteDto) throws IamportResponseException, IOException {
        try {
            orderService.completeOrder(orderCompleteDto);
            log.info("Payment completed for order: {}", orderCompleteDto);
            return ResponseEntity.status(HttpStatus.OK).body(Map.of("message", "결제 성공"));
        } catch (Exception e) {
            // 결제 실패 시 결제 취소 및 주문 삭제 처리
            orderService.cancelPaymentAndDeleteOrder(orderCompleteDto);
            log.error("Unexpected error completing order: {}", orderCompleteDto, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "서버 오류", "error", e.getMessage()));
        }
    }

    //결제취소
    @PostMapping("/cancel")
    public ResponseEntity orderCancel(@RequestBody CancelOrderRequestDto request) throws JsonProcessingException {
        // 결제 취소
        orderService.cancelPayment(request.getOrderId(), request.getAmount(), request.getReason());
        // 주문상품 상태를 취소로 변경
        orderService.cancelOrderProduct(request.getOrderProductId());
        return ResponseEntity.status(HttpStatus.OK).body(Map.of("message", "취소 성공"));
    }
}
