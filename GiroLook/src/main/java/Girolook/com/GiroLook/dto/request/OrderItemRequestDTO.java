package Girolook.com.GiroLook.dto.request;


import java.util.UUID;

public record OrderItemRequestDTO(
        UUID productId,
        Integer quantity
){}
