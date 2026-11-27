package personal_finance_manager_backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record TransactionRequest(
        @NotBlank(message = "Transaction type is required.")
        @Pattern(regexp = "(?i)INCOME|EXPENSE", message = "Type must be INCOME or EXPENSE.")
        String type,

        @NotNull(message = "Amount is required.")
        @DecimalMin(value = "0.01", message = "Amount must be greater than zero.")
        BigDecimal amount,

        @NotBlank(message = "Category is required.")
        @Size(max = 80, message = "Category must not exceed 80 characters.")
        String category,

        @Size(max = 500, message = "Description must not exceed 500 characters.")
        String description,

        @NotNull(message = "Transaction date is required.")
        LocalDate transactionDate
) {}
