package Girolook.com.GiroLook.dto.response;

import jakarta.persistence.Column;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserResponseDTO( UUID id,

                               String name,

                               String email,

                               String phoneNumber,

                               String address
 ) {


}
