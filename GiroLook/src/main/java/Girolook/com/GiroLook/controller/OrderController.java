package Girolook.com.GiroLook.controller;

import Girolook.com.GiroLook.dto.request.OrderRequestDTO;
import Girolook.com.GiroLook.dto.response.OrderResponseDTO;
import Girolook.com.GiroLook.models.User;
import Girolook.com.GiroLook.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }


    @PostMapping
    public ResponseEntity<OrderResponseDTO> createOrder(
            @RequestBody @Valid OrderRequestDTO request,
            @AuthenticationPrincipal User currentUser) {

        OrderResponseDTO response = orderService.createOrder(request, currentUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    @PatchMapping("/{id}/confirm")
    public ResponseEntity<Void> confirmOrder(
            @PathVariable UUID id,
            @AuthenticationPrincipal User currentUser) {

        orderService.confirmOrder(id,  currentUser.getId());
        return ResponseEntity.noContent().build();
    }


    @PatchMapping("/{id}/ship")
    public ResponseEntity<Void> shipOrder(
            @PathVariable UUID id,
            @AuthenticationPrincipal User currentUser) {

        orderService.deliverOrder(id,  currentUser.getId());
        return ResponseEntity.noContent().build();
    }


    @PatchMapping("/{id}/deliver")
    public ResponseEntity<Void> confirmDelivery(
            @PathVariable UUID id,
            @AuthenticationPrincipal User currentUser) {

        orderService.confirmDeliveryByCustomer(id, currentUser.getId());
        return ResponseEntity.noContent().build();
    }


    @PatchMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelOrder(
            @PathVariable UUID id,
            @AuthenticationPrincipal User currentUser) {

        orderService.cancelOrder(id, currentUser.getId());
        return ResponseEntity.noContent().build();
    }
}
