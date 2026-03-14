package Girolook.com.GiroLook.dto.response;



import java.util.List;
import java.util.UUID;


public record StoreResponseDTO(UUID id,
                               String name,
                               String description,
                               String logoUrl,
                               List<ProductResponseDTO> products){
}
