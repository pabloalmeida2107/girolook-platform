package Girolook.com.GiroLook.dto.response;

import java.util.UUID;

public record StoreSummaryResponseDTO(
        UUID id,
        String name,
        String logoUrl
) {
}