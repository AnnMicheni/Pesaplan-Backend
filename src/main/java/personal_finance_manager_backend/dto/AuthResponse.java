
package personal_finance_manager_backend.dto;

import java.time.LocalDateTime;

public record AuthResponse(
        String message,
        String accessToken,
        String tokenType,
        Long id,
        String fullName,
        String email,
        String currencyPreference,
        LocalDateTime createdAt
) {
}
