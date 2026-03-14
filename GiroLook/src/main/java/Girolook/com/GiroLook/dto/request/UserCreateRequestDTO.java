package Girolook.com.GiroLook.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserCreateRequestDTO(@NotBlank(message = "O nome não pode estar em branco")
                                   @Size(min = 3, max = 100, message = "O nome deve ter entre 3 e 100 caracteres")
                                   String name,

                                   @NotBlank(message = "O e-mail é obrigatório")
                                   @Email(message = "O e-mail deve ser válido")
                                   String email,

                                   @NotBlank(message = "A senha é obrigatória")
                                   @Size(min = 8, message = "A senha deve ter no mínimo 8 caracteres")
                                   String password,

                                   @NotBlank(message = "O telefone é obrigatório")
                                   String phoneNumber,

                                   String address) {
}
