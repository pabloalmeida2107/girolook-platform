package Girolook.com.GiroLook.dto.request;

import jakarta.validation.constraints.NotNull;

public record StoreRequestDTO(
                            @NotNull
                            String name,
                            String description,
                            String logoUrl) {
}
