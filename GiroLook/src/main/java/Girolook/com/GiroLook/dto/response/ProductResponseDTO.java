package Girolook.com.GiroLook.dto.response;

import Girolook.com.GiroLook.models.enums.Category;
import Girolook.com.GiroLook.models.enums.ProductStatus;
import Girolook.com.GiroLook.models.enums.ProductType;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductResponseDTO(
        UUID id,
        UUID storeId,
        String storeName,
        String name,
        String description,
        BigDecimal price,
        String size,
        Category category,
        String color,
        String imageUrl,
        ProductStatus status,
        ProductType type
) {
}
