package personal_finance_manager_backend.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import personal_finance_manager_backend.dto.TransactionRequest;
import personal_finance_manager_backend.model.Transaction;
import personal_finance_manager_backend.model.Income;
import personal_finance_manager_backend.model.Expense;
import personal_finance_manager_backend.repository.TransactionRepository;
import personal_finance_manager_backend.repository.TransactionRepository.CategoryTotal;

@Service
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final UserService userService;

    public TransactionService(TransactionRepository transactionRepository, UserService userService) {
        this.transactionRepository = transactionRepository;
        this.userService = userService;
    }

    public Transaction create(String email, TransactionRequest request) {
        Transaction item = fromRequest(request);
        item.setUserId(resolveUserId(email));
        return transactionRepository.save(item);
    }

    public List<Transaction> findAll(String email, String type, String category,
                                               LocalDate fromDate, LocalDate toDate) {
        validateDateRange(fromDate, toDate);
        validateType(type);
        return transactionRepository.findAll(resolveUserId(email), type, category, fromDate, toDate);
    }

    public Transaction findOne(String email, Long id) {
        return transactionRepository.findByIdAndUserId(id, resolveUserId(email))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transaction not found."));
    }

    public Transaction update(String email, Long id, TransactionRequest request) {
        Long userId = resolveUserId(email);
        Transaction item = fromRequest(request);
        item.setUserId(userId);
        if (!transactionRepository.update(id, userId, item)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Transaction not found.");
        }
        return findOne(email, id);
    }

    public void delete(String email, Long id) {
        if (!transactionRepository.delete(id, resolveUserId(email))) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Transaction not found.");
        }
    }

    public List<String> categories(String email) {
        return transactionRepository.findCategories(resolveUserId(email));
    }

    public List<CategoryTotal> categoryTotals(String email, String type,
                                               LocalDate fromDate, LocalDate toDate) {
        validateType(type);
        validateDateRange(fromDate, toDate);
        return transactionRepository.categoryTotals(resolveUserId(email), type, fromDate, toDate);
    }

    private Long resolveUserId(String email) {
        return userService.findUserByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found."))
                .getId();
    }

    private Transaction fromRequest(TransactionRequest request) {
        Transaction item = request.type().equalsIgnoreCase("INCOME") ? new Income() : new Expense();
        item.setType(request.type().toUpperCase());
        item.setAmount(request.amount());
        item.setCategory(request.category().trim());
        item.setDescription(request.description() == null ? null : request.description().trim());
        item.setTransactionDate(request.transactionDate());
        return item;
    }

    private void validateType(String type) {
        if (type != null && !type.isBlank()
                && !type.equalsIgnoreCase("INCOME") && !type.equalsIgnoreCase("EXPENSE")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Type must be INCOME or EXPENSE.");
        }
    }

    private void validateDateRange(LocalDate fromDate, LocalDate toDate) {
        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "fromDate must be on or before toDate.");
        }
    }
}
