package personal_finance_manager_backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record TransactionResponse(
        Long id,
        String type,
        BigDecimal amount,
        String category,
        String description,
        LocalDate transactionDate,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
