package personal_finance_manager_backend.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import personal_finance_manager_backend.dto.TransactionRequest;
import personal_finance_manager_backend.dto.TransactionResponse;
import personal_finance_manager_backend.model.Transaction;
import personal_finance_manager_backend.service.TransactionService;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {
    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping
    public ResponseEntity<TransactionResponse> create(
            Authentication authentication, @Valid @RequestBody TransactionRequest request) {
        Transaction saved = transactionService.create(authentication.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(saved));
    }

    @GetMapping
    public ResponseEntity<List<TransactionResponse>> findAll(
            Authentication authentication,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) LocalDate fromDate,
            @RequestParam(required = false) LocalDate toDate) {
        List<TransactionResponse> result = transactionService
                .findAll(authentication.getName(), type, category, fromDate, toDate)
                .stream().map(this::toResponse).toList();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/categories")
    public ResponseEntity<List<String>> categories(Authentication authentication) {
        return ResponseEntity.ok(transactionService.categories(authentication.getName()));
    }

    @GetMapping("/category-totals")
    public ResponseEntity<?> categoryTotals(
            Authentication authentication,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) LocalDate fromDate,
            @RequestParam(required = false) LocalDate toDate) {
        return ResponseEntity.ok(transactionService.categoryTotals(
                authentication.getName(), type, fromDate, toDate));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponse> findOne(
            Authentication authentication, @PathVariable Long id) {
        return ResponseEntity.ok(toResponse(transactionService.findOne(authentication.getName(), id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TransactionResponse> update(
            Authentication authentication, @PathVariable Long id,
            @Valid @RequestBody TransactionRequest request) {
        return ResponseEntity.ok(toResponse(
                transactionService.update(authentication.getName(), id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(Authentication authentication, @PathVariable Long id) {
        transactionService.delete(authentication.getName(), id);
        return ResponseEntity.noContent().build();
    }

    private TransactionResponse toResponse(Transaction item) {
        return new TransactionResponse(item.getId(), item.getType(), item.getAmount(),
                item.getCategory(), item.getDescription(), item.getTransactionDate(),
                item.getCreatedAt(), item.getUpdatedAt());
    }
}
