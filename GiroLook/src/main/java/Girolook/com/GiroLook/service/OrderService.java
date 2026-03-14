package Girolook.com.GiroLook.service;

import Girolook.com.GiroLook.dto.request.OrderRequestDTO;
import Girolook.com.GiroLook.dto.response.OrderResponseDTO;

import Girolook.com.GiroLook.exceptions.BusinessException;
import Girolook.com.GiroLook.exceptions.ResourceNotFoundException;
import Girolook.com.GiroLook.exceptions.UnauthorizedAccessException;
import Girolook.com.GiroLook.models.*;
import Girolook.com.GiroLook.models.enums.OrderStatus;
import Girolook.com.GiroLook.models.enums.OrderType;
import Girolook.com.GiroLook.repository.OrderRepository;
import Girolook.com.GiroLook.repository.ProductRepository;

import Girolook.com.GiroLook.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;

import java.util.List;
import java.util.UUID;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public OrderService(OrderRepository orderRepository, UserRepository userRepository, ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public OrderResponseDTO createOrder(OrderRequestDTO request, UUID userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));


        Order order = new Order();
        order.setUser(user);
        order.setOrderStatus(OrderStatus.WAITING_CONFIRMATION);
        order.setOrderType(request.orderType());

        if (request.orderType() == OrderType.RENTAL) {
            order.setRentalStartDate(request.rentalStartDate());
            order.setRentalEndDate(request.rentalEndDate());
        }


        BigDecimal calculatedTotal = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (var itemRequest : request.items()) {
            Product product = productRepository.findById(itemRequest.productId())
                    .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado: " + itemRequest.productId()));

            if (!product.isActive()) {
                throw new BusinessException("Produto " + product.getName() + " não está disponível.");
            }


            if (product.getStore().getOwner().getId().equals(userId)) {
                throw new UnauthorizedAccessException("Você não pode comprar produtos da sua própria loja.");
            }


            if (order.getStore() == null) {
                order.setStore(product.getStore());
            }


            OrderItem item = new OrderItem();
            item.setProduct(product);
            item.setOrder(order);
            item.setPriceAtPurchase(product.getPrice());
            item.setQuantity(itemRequest.quantity());

            orderItems.add(item);


            BigDecimal itemTotal = product.getPrice().multiply(BigDecimal.valueOf(itemRequest.quantity()));
            calculatedTotal = calculatedTotal.add(itemTotal);
        }

        order.setItems(orderItems);
        order.setTotalAmount(calculatedTotal);

        Order savedOrder = orderRepository.save(order);
        return toResponse(savedOrder);
    }

    @Transactional
    public void confirmOrder(UUID orderId, UUID authenticatedUserId) {
        Order order = findOrder(orderId);
        validateStoreOwner(order, authenticatedUserId, "confirmar");

        if (order.getOrderStatus() != OrderStatus.WAITING_CONFIRMATION) {
            throw new BusinessException("Status inválido para confirmação.");
        }

        order.setOrderStatus(OrderStatus.CONFIRMED);
    }

    @Transactional
    public void deliverOrder(UUID orderId, UUID authenticatedUserId) {
        Order order = findOrder(orderId);
        validateStoreOwner(order, authenticatedUserId, "enviar");

        if (order.getOrderStatus() != OrderStatus.CONFIRMED) {
            throw new BusinessException("O pedido precisa estar confirmado.");
        }

        order.setOrderStatus(OrderStatus.DELIVERED);
    }

    @Transactional
    public void confirmDeliveryByCustomer(UUID orderId, UUID authenticatedUserId) {
        Order order = findOrder(orderId);

        if (!order.getUser().getId().equals(authenticatedUserId)) {
            throw new UnauthorizedAccessException("Apenas o comprador confirma o recebimento.");
        }

        if (order.getOrderStatus() != OrderStatus.DELIVERED) {
            throw new BusinessException("O pedido ainda não foi enviado.");
        }

        order.setOrderStatus(OrderStatus.COMPLETED);


        if (order.getOrderType() == OrderType.PURCHASE) {
            order.getItems().forEach(item -> item.getProduct().setActive(false));
        }
    }

    @Transactional
    public void cancelOrder(UUID orderId, UUID authenticatedUserId) {
        Order order = findOrder(orderId);

        boolean isOwner = order.getStore().getOwner().getId().equals(authenticatedUserId);
        boolean isCustomer = order.getUser().getId().equals(authenticatedUserId);

        if (!isOwner && !isCustomer) {
            throw new UnauthorizedAccessException("Sem permissão para cancelar.");
        }

        if (order.getOrderStatus() == OrderStatus.DELIVERED || order.getOrderStatus() == OrderStatus.COMPLETED) {
            throw new BusinessException("Impossível cancelar pedido já enviado ou finalizado.");
        }

        order.setOrderStatus(OrderStatus.CANCELED);
    }



    private Order findOrder(UUID id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido não encontrado"));
    }

    private void validateStoreOwner(Order order, UUID userId, String acao) {
        if (!order.getStore().getOwner().getId().equals(userId)) {
            throw new UnauthorizedAccessException("Apenas o dono da loja pode " + acao + " o pedido.");
        }
    }

    private OrderResponseDTO toResponse(Order order) {

        List<String> productNames = order.getItems().stream()
                .map(item -> item.getProduct().getName())
                .toList();

        return new OrderResponseDTO(
                order.getId(),
                order.getUser().getName(),
                productNames,
                order.getStore().getName(),
                order.getTotalAmount(),
                order.getOrderStatus(),
                order.getOrderType(),
                order.getRentalStartDate(),
                order.getRentalEndDate(),
                order.getCreatedAt()
        );
    }
}