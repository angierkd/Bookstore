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
import com.siot.IamportRestClient.IamportClient;
import com.siot.IamportRestClient.exception.IamportResponseException;
import com.siot.IamportRestClient.response.IamportResponse;
import com.siot.IamportRestClient.response.Payment;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class OrderService {

    private final OrdersRepository orderRepository;
    private final OrderProductRepository orderProductRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final OrderCancelRepository orderCancelRepository;
    private final IamportClient iamportClient; // 아임포트 API 클라이언트
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private static final String IAMPORT_API_URL = "https://api.iamport.kr";

    @Value("${imp.api.key}")
    private String IAMPORT_API_KEY;

    @Value("${imp.api.secretkey}")
    private String IAMPORT_API_SECRET;


    // 주문서
    public List<Cart> getBill(Long userId) {
        List<Cart> cartList = cartRepository.findByUserId(userId);
        return cartList;
    }

    public List<Orders> getOrderList(Long id) {
        List<Orders> orders = orderRepository.findAllByUserIdAndStatus(id);
        return orders;
    }

    // 주문 생성
    @Transactional
    public Orders createOrder(OrderCreateDto orderCreateDto) {
        List<Cart> carts = cartRepository.findAllById(orderCreateDto.getCartIds());
        Orders order = orderCreateDto.convertToOrders(carts);
        orderRepository.save(order);

        List<OrderProduct> orderProducts = orderCreateDto.convertToOrderProduct(carts, order);
        orderProductRepository.saveAll(orderProducts);
        return order;
    }

    @Transactional
    public void completeOrder(OrderCompleteDto orderCompleteDto) throws IamportResponseException, IOException {

        IamportResponse<Payment> paymentResponse = iamportClient.paymentByImpUid(orderCompleteDto.getImpUid());

        if (paymentResponse.getResponse().getStatus().equals("paid")) {
            Orders order = orderCompleteDto.getOrder();

            // 상태를 결제완료로 변경
            order.setStatus(true);
            orderRepository.save(order);

            // 재고 감소 & 장바구니 삭제
            reduceStockAndClearCart(order);
        }else{
            throw new RuntimeException("Payment not completed");
        }
    }

    @Transactional
    public void reduceStockAndClearCart(Orders order) {
        List<OrderProduct> orderProducts = orderProductRepository.findAllByOrders(order);
        orderProducts.forEach(orderProduct -> {

            reduceProduct(orderProduct);

            //장바구니 삭제
            cartRepository.deleteByProductIdAndUserId(orderProduct.getProduct().getId(), order.getUser().getId());
        });
    }



    @Transactional
    public void reduceProduct(OrderProduct orderProduct) {
            Product product = productRepository.findByIdWithPessimisticLock(orderProduct.getProduct().getId())
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            // 재고 감소
            int newStock = product.getStock() - orderProduct.getQuantity();
            product.setStock(newStock);
            productRepository.save(product);
    }

    // 전체 주문 최소
    @Transactional
    public void cancelPaymentAndDeleteOrder(OrderCompleteDto orderCompleteDto) throws JsonProcessingException {
        Orders order = orderCompleteDto.getOrder();
        //결제 취소
        cancelPayment(Long.valueOf(orderCompleteDto.getImpUid()), order.getTotalPrice(), "오류로 인한 결제 취소");

        // 주문 및 주문 상품 테이블 삭제
        orderProductRepository.deleteByOrders(order);
        orderRepository.deleteById(order.getId());
    }

    // 개별 주문 상품 취소
    @Transactional
    public void cancelOrderProduct(Long orderProductId) {
        OrderProduct orderProduct = orderProductRepository.findById(orderProductId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found with id " + orderProductId));

        //상태를 주문취소로 변경
        orderProduct.setStatus(true);
        orderProductRepository.save(orderProduct);

        //orderCancel 테이블에 추가
        OrderCancel cancel = OrderCancel.builder()
                .ordersProduct(orderProduct)
                .build();
        orderCancelRepository.save(cancel);
    }

    // 토큰 발급
    public String getAccessToken() {
        String url = IAMPORT_API_URL + "/users/getToken";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String requestJson = "{\"imp_key\":\"" + IAMPORT_API_KEY + "\", \"imp_secret\":\"" + IAMPORT_API_SECRET + "\"}";

        HttpEntity<String> entity = new HttpEntity<>(requestJson, headers);
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);

        Map<String, Object> responseBody = response.getBody();
        if (responseBody != null && responseBody.get("response") != null) {
            Map<String, String> responseMap = (Map<String, String>) responseBody.get("response");
            return responseMap.get("access_token");
        }

        throw new RuntimeException("Iamport 토큰 발급 실패");
    }

    // 결제 부분 취소
    public Map<String, Object> cancelPayment(Long impUid, int amount, String reason) throws JsonProcessingException {

        String url = IAMPORT_API_URL + "/payments/cancel";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(getAccessToken());

        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("merchant_uid", impUid);
        requestMap.put("amount", amount);
        requestMap.put("reason", reason);

        String jsonRequestBody = objectMapper.writeValueAsString(requestMap);

        HttpEntity<String> entity = new HttpEntity<>(jsonRequestBody, headers);

        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);

        Map<String, Object> responseBody = response.getBody();

        return responseBody;
    }

}
