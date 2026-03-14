package Girolook.com.GiroLook.dto.request;

import Girolook.com.GiroLook.models.enums.OrderType;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Positive;
import org.antlr.v4.runtime.misc.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record OrderRequestDTO(
        @NotNull
        List<OrderItemRequestDTO> items,

        @NotNull
        UUID storeId,

        @NotNull
        OrderType orderType,

        @NotNull
        @Positive(message = "O valor pago deve ser maior que zero")
        BigDecimal totalAmount,

        @FutureOrPresent
        LocalDate rentalStartDate,
        @FutureOrPresent
        LocalDate rentalEndDate
) {}
