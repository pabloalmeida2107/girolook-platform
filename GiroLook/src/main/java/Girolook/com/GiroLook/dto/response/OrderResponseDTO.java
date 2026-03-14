package Girolook.com.GiroLook.dto.response;

import Girolook.com.GiroLook.models.enums.OrderStatus;
import Girolook.com.GiroLook.models.enums.OrderType;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OrderResponseDTO(
        UUID id,
        String userName,
        List<String> productNames,
        String storeName,
        BigDecimal totalAmount,
        OrderStatus status,
        OrderType orderType,
        LocalDate rentalStartDate,
        LocalDate rentalEndDate,
        LocalDateTime createdAt
) {}
