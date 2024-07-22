package com.shopping.book.order.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopping.book.cart.repository.CartRepository;
import com.shopping.book.order.config.AppConfig;
import com.shopping.book.order.dto.OrderCompleteDto;
import com.shopping.book.order.entity.OrderProduct;
import com.shopping.book.order.entity.Orders;
import com.shopping.book.order.repository.OrderCancelRepository;
import com.shopping.book.order.repository.OrderProductRepository;
import com.shopping.book.order.repository.OrdersRepository;
import com.shopping.book.product.entity.Product;
import com.shopping.book.product.repository.ProductRepository;
import com.siot.IamportRestClient.IamportClient;
import org.junit.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.shaded.com.fasterxml.jackson.core.JsonProcessingException;

import java.net.URISyntaxException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RestClientTest(OrderService.class)
@Import(AppConfig.class)  // RestTemplateConfig를 Import하여 RestTemplate 빈을 사용
public class OrderServiceRestTemplateTest {


    @MockBean
    private OrderProductRepository orderProductRepository;

    @MockBean
    private OrdersRepository orderRepository;

    @MockBean
    private CartRepository cartRepository;

    @MockBean
    private ProductRepository productRepository;

    @MockBean
    private IamportClient iamportClient; // 아임포트 API 클라이언트

    @MockBean
    private OrderCancelRepository orderCancelRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private OrderService orderService;

    private MockRestServiceServer server;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RestTemplateBuilder restTemplateBuilder;

    String successResult;
    String errorResult;

    @BeforeEach
    public void setup() {
        server = MockRestServiceServer.createServer(restTemplate);

        successResult = "{\n" +
                "    \"response\": {\n" +
                "        \"access_token\": \"test_access_token\"\n" +
                "    }\n" +
                "}";

        errorResult =  "{\n" +
                "    \"error\": \"invalid_request\",\n" +
                "    \"message\": \"Invalid API key or secret\"\n" +
                "}";
    }

    @Test
    @DisplayName("오류 발생에 따른, 전체 주문 최소 테스트")
    public void cancelPaymentAndDeleteOrderTest() throws com.fasterxml.jackson.core.JsonProcessingException {

        //given
        server.expect(requestTo("https://api.iamport.kr/users/getToken"))
                .andRespond(withSuccess(successResult, MediaType.APPLICATION_JSON));

        server.expect(requestTo("https://api.iamport.kr/payments/cancel"))
                .andRespond(withSuccess(successResult, MediaType.APPLICATION_JSON));

        Orders order = Orders.builder().id(1L).build();
        OrderCompleteDto orderCompleteDto = new OrderCompleteDto("1", order);

        //when
        orderService.cancelPaymentAndDeleteOrder(orderCompleteDto);

        //then
        verify(orderProductRepository, times(1)).deleteByOrders(order);
        verify(orderRepository, times(1)).deleteById(order.getId());

    }

    @Test
    @DisplayName("토큰 발급 실패 테스트")
    public void getAccessTokenTest_Fail() throws JsonProcessingException, URISyntaxException, com.fasterxml.jackson.core.JsonProcessingException {
        //given
        server.expect(requestTo("https://api.iamport.kr/users/getToken"))
                .andRespond(withSuccess(errorResult, MediaType.APPLICATION_JSON));

        // when & then
        assertThatThrownBy(() -> orderService.getAccessToken())
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Iamport 토큰 발급 실패");
    }

    @Test
    @DisplayName("찾는 상품 없을 때 오류 발생 테스트")
    public void reduceProductTest_ProductNotFound() {
        // given
        Product product = Product.builder().id(1L).build();
        OrderProduct orderProduct = OrderProduct.builder().id(1L).build();

        when(productRepository.findByIdWithPessimisticLock(orderProduct.getId())).thenReturn(java.util.Optional.empty());

        // when & then
        assertThrows(RuntimeException.class, () -> orderService.reduceProduct(orderProduct));
        verify(productRepository, never()).save(product);
    }
}