package Girolook.com.GiroLook.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserPasswordUpdateRequestDTO(
        @NotBlank(message = "A senha atual é obrigatória")
        String oldPassword,

        @NotBlank(message = "A nova senha é obrigatória")
        @Size(min = 8, message = "A nova senha deve ter no mínimo 8 caracteres")
        String newPassword
) {
}

