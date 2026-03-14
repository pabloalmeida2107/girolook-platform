package Girolook.com.GiroLook.dto.response;

import Girolook.com.GiroLook.models.enums.ProductType;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductSummaryResponseDTO(
        UUID id,
        String name,
        BigDecimal price,
        String imageUrl,
        ProductType type,
        String storeName
) {
}
