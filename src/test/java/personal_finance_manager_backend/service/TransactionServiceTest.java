package personal_finance_manager_backend.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import personal_finance_manager_backend.dto.TransactionRequest;
import personal_finance_manager_backend.model.User;
import personal_finance_manager_backend.model.Transaction;
import personal_finance_manager_backend.repository.TransactionRepository;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {
    @Mock private TransactionRepository transactionRepository;
    @Mock private UserService userService;
    @InjectMocks private TransactionService transactionService;

    private User testUser() {
        User user = new User();
        user.setId(17L);
        user.setEmail("test@example.com");
        return user;
    }

    private TransactionRequest expenseRequest() {
        return new TransactionRequest("EXPENSE", new BigDecimal("125.50"),
                "Food", "Lunch", LocalDate.of(2026, 11, 27));
    }

    @Test
    void createsExpenseForAuthenticatedUser() {
        when(userService.findUserByEmail("test@example.com")).thenReturn(Optional.of(testUser()));
        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Transaction saved = transactionService.create("test@example.com", expenseRequest());

        assertEquals(17L, saved.getUserId());
        assertEquals("EXPENSE", saved.getType());
        assertEquals(new BigDecimal("125.50"), saved.getAmount());
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void listsTransactionsForAuthenticatedUserWithFilters() {
        when(userService.findUserByEmail("test@example.com")).thenReturn(Optional.of(testUser()));
        when(transactionRepository.findAll(eq(17L), eq("EXPENSE"), eq("Food"), any(), any()))
                .thenReturn(List.of());

        List<Transaction> result = transactionService.findAll("test@example.com", "EXPENSE",
                "Food", LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 30));

        assertTrue(result.isEmpty());
        verify(transactionRepository).findAll(eq(17L), eq("EXPENSE"), eq("Food"), any(), any());
    }

    @Test
    void rejectsInvalidTransactionTypeFilter() {
        assertThrows(ResponseStatusException.class, () -> transactionService.findAll(
                "test@example.com", "TRANSFER", null, null, null));
        verifyNoInteractions(transactionRepository, userService);
    }

    @Test
    void rejectsReversedDateRange() {
        assertThrows(ResponseStatusException.class, () -> transactionService.findAll(
                "test@example.com", null, null,
                LocalDate.of(2026, 11, 30), LocalDate.of(2026, 11, 1)));
        verifyNoInteractions(transactionRepository, userService);
    }

    @Test
    void returnsNotFoundWhenTransactionDoesNotBelongToUser() {
        when(userService.findUserByEmail("test@example.com")).thenReturn(Optional.of(testUser()));
        when(transactionRepository.findByIdAndUserId(999L, 17L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class,
                () -> transactionService.findOne("test@example.com", 999L));
    }

    @Test
    void deletesOnlyUsersOwnTransaction() {
        when(userService.findUserByEmail("test@example.com")).thenReturn(Optional.of(testUser()));
        when(transactionRepository.delete(5L, 17L)).thenReturn(true);

        assertDoesNotThrow(() -> transactionService.delete("test@example.com", 5L));
        verify(transactionRepository).delete(5L, 17L);
    }
}
