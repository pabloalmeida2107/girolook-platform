package Girolook.com.GiroLook.dto.request;

import Girolook.com.GiroLook.models.Store;
import Girolook.com.GiroLook.models.enums.Category;
import Girolook.com.GiroLook.models.enums.ProductStatus;
import Girolook.com.GiroLook.models.enums.ProductType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record ProductRequestDTO(

        @NotBlank(message = "O nome é obrigatório")
        String name,

        String description,

        @NotNull(message = "O preço é obrigatório")
        @Positive(message = "O preço deve ser maior que zero")
        BigDecimal price,

        @NotBlank(message = "O tamanho é obrigatório")
        String size,

        @NotNull(message = "A categoria é obrigatória")
        Category category,

        String color,

        String imageUrl,

        @NotNull(message = "O tipo (ALUGUEL/VENDA) é obrigatório")
        ProductType type,

        @NotNull(message = "O ID da loja é obrigatório")
        UUID storeId
) {
}
