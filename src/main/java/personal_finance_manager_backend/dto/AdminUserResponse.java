
package personal_finance_manager_backend.dto;

import java.time.LocalDateTime;

public record AdminUserResponse(
        Long id,
        String fullName,
        String email,
        String currencyPreference,
        String role,
        boolean active,
        LocalDateTime createdAt
) {
}
