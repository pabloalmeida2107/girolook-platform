package Girolook.com.GiroLook.service;

import Girolook.com.GiroLook.dto.request.OrderRequestDTO;
import Girolook.com.GiroLook.dto.request.OrderItemRequestDTO;
import Girolook.com.GiroLook.exceptions.*;
import Girolook.com.GiroLook.models.*;
import Girolook.com.GiroLook.models.enums.OrderStatus;
import Girolook.com.GiroLook.models.enums.OrderType;
import Girolook.com.GiroLook.repository.OrderRepository;
import Girolook.com.GiroLook.repository.ProductRepository;
import Girolook.com.GiroLook.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private OrderService orderService;

    private UUID userId;
    private UUID storeId;
    private User user;
    private Product product;
    private Store store;

    @BeforeEach
    void setup() {
        userId = UUID.randomUUID();
        storeId = UUID.randomUUID();

        user = new User();
        user.setId(userId);
        user.setName("Pablo");

        User storeOwner = new User();
        storeOwner.setId(UUID.randomUUID());

        store = new Store();
        store.setId(storeId);
        store.setOwner(storeOwner);
        store.setName("Loja Teste");

        product = new Product();
        product.setId(UUID.randomUUID());
        product.setName("Produto 1");
        product.setPrice(BigDecimal.valueOf(100));
        product.setActive(true);
        product.setStore(store);
    }



    @Test
    void createOrder_success() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        OrderItemRequestDTO item = new OrderItemRequestDTO(product.getId(), 2);

        OrderRequestDTO request = new OrderRequestDTO(
                List.of(item),
                storeId,
                OrderType.PURCHASE,
                BigDecimal.valueOf(200),
                null,
                null
        );

        var response = orderService.createOrder(request, userId);

        assertNotNull(response);
        assertEquals(BigDecimal.valueOf(200), response.totalAmount());
    }

    @Test
    void createOrder_userNotFound() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        OrderRequestDTO request = new OrderRequestDTO(
                List.of(),
                storeId,
                OrderType.PURCHASE,
                BigDecimal.ONE,
                null,
                null
        );

        assertThrows(ResourceNotFoundException.class,
                () -> orderService.createOrder(request, userId));
    }

    @Test
    void createOrder_productNotFound() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(productRepository.findById(any())).thenReturn(Optional.empty());

        OrderItemRequestDTO item = new OrderItemRequestDTO(UUID.randomUUID(), 1);

        OrderRequestDTO request = new OrderRequestDTO(
                List.of(item),
                storeId,
                OrderType.PURCHASE,
                BigDecimal.valueOf(100),
                null,
                null
        );

        assertThrows(ResourceNotFoundException.class,
                () -> orderService.createOrder(request, userId));
    }

    @Test
    void createOrder_productInactive() {
        product.setActive(false);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));

        OrderItemRequestDTO item = new OrderItemRequestDTO(product.getId(), 1);

        OrderRequestDTO request = new OrderRequestDTO(
                List.of(item),
                storeId,
                OrderType.PURCHASE,
                BigDecimal.valueOf(100),
                null,
                null
        );

        assertThrows(BusinessException.class,
                () -> orderService.createOrder(request, userId));
    }

    @Test
    void createOrder_buyOwnProduct() {
        store.getOwner().setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));

        OrderItemRequestDTO item = new OrderItemRequestDTO(product.getId(), 1);

        OrderRequestDTO request = new OrderRequestDTO(
                List.of(item),
                storeId,
                OrderType.PURCHASE,
                BigDecimal.valueOf(100),
                null,
                null
        );

        assertThrows(UnauthorizedAccessException.class,
                () -> orderService.createOrder(request, userId));
    }



    @Test
    void confirmOrder_success() {
        Order order = new Order();
        order.setOrderStatus(OrderStatus.WAITING_CONFIRMATION);
        order.setStore(store);

        when(orderRepository.findById(any())).thenReturn(Optional.of(order));

        orderService.confirmOrder(UUID.randomUUID(), store.getOwner().getId());

        assertEquals(OrderStatus.CONFIRMED, order.getOrderStatus());
    }

    @Test
    void confirmOrder_invalidStatus() {
        Order order = new Order();
        order.setOrderStatus(OrderStatus.CONFIRMED);
        order.setStore(store);

        when(orderRepository.findById(any())).thenReturn(Optional.of(order));

        assertThrows(BusinessException.class,
                () -> orderService.confirmOrder(UUID.randomUUID(), store.getOwner().getId()));
    }



    @Test
    void deliverOrder_success() {
        Order order = new Order();
        order.setOrderStatus(OrderStatus.CONFIRMED);
        order.setStore(store);

        when(orderRepository.findById(any())).thenReturn(Optional.of(order));

        orderService.deliverOrder(UUID.randomUUID(), store.getOwner().getId());

        assertEquals(OrderStatus.DELIVERED, order.getOrderStatus());
    }

    @Test
    void deliverOrder_invalidStatus() {
        Order order = new Order();
        order.setOrderStatus(OrderStatus.WAITING_CONFIRMATION);
        order.setStore(store);

        when(orderRepository.findById(any())).thenReturn(Optional.of(order));

        assertThrows(BusinessException.class,
                () -> orderService.deliverOrder(UUID.randomUUID(), store.getOwner().getId()));
    }



    @Test
    void confirmDelivery_success() {
        Order order = new Order();
        order.setOrderStatus(OrderStatus.DELIVERED);
        order.setUser(user);
        order.setOrderType(OrderType.PURCHASE);

        OrderItem item = new OrderItem();
        item.setProduct(product);
        order.setItems(List.of(item));

        when(orderRepository.findById(any())).thenReturn(Optional.of(order));

        orderService.confirmDeliveryByCustomer(UUID.randomUUID(), userId);

        assertEquals(OrderStatus.COMPLETED, order.getOrderStatus());
        assertFalse(product.isActive());
    }

    @Test
    void confirmDelivery_wrongUser() {
        Order order = new Order();
        order.setUser(user);

        when(orderRepository.findById(any())).thenReturn(Optional.of(order));

        assertThrows(UnauthorizedAccessException.class,
                () -> orderService.confirmDeliveryByCustomer(UUID.randomUUID(), UUID.randomUUID()));
    }



    @Test
    void cancelOrder_byOwner() {
        Order order = new Order();
        order.setOrderStatus(OrderStatus.WAITING_CONFIRMATION);
        order.setUser(user);
        order.setStore(store);

        when(orderRepository.findById(any())).thenReturn(Optional.of(order));

        orderService.cancelOrder(UUID.randomUUID(), store.getOwner().getId());

        assertEquals(OrderStatus.CANCELED, order.getOrderStatus());
    }

    @Test
    void cancelOrder_invalidStatus() {
        Order order = new Order();
        order.setOrderStatus(OrderStatus.DELIVERED);
        order.setUser(user);
        order.setStore(store);

        when(orderRepository.findById(any())).thenReturn(Optional.of(order));

        assertThrows(BusinessException.class,
                () -> orderService.cancelOrder(UUID.randomUUID(), store.getOwner().getId()));
    }

    @Test
    void cancelOrder_unauthorized() {
        Order order = new Order();
        order.setUser(user);
        order.setStore(store);

        when(orderRepository.findById(any())).thenReturn(Optional.of(order));

        assertThrows(UnauthorizedAccessException.class,
                () -> orderService.cancelOrder(UUID.randomUUID(), UUID.randomUUID()));
    }
}