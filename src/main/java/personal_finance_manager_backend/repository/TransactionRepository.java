package personal_finance_manager_backend.repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import personal_finance_manager_backend.model.Transaction;
import personal_finance_manager_backend.model.Income;
import personal_finance_manager_backend.model.Expense;

@Repository
public class TransactionRepository {
    private final JdbcTemplate jdbcTemplate;

    public TransactionRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Transaction save(Transaction transaction) {
        String sql = """
                INSERT INTO transactions
                    (user_id, type, amount, category, description, transaction_date)
                VALUES (?, ?, ?, ?, ?, ?)
                RETURNING id, user_id, type, amount, category, description,
                          transaction_date, created_at, updated_at
                """;
        return jdbcTemplate.queryForObject(sql, mapper,
                transaction.getUserId(), transaction.getType(), transaction.getAmount(),
                transaction.getCategory(), transaction.getDescription(), transaction.getTransactionDate());
    }

    public Optional<Transaction> findByIdAndUserId(Long id, Long userId) {
        String sql = """
                SELECT id, user_id, type, amount, category, description,
                       transaction_date, created_at, updated_at
                FROM transactions WHERE id = ? AND user_id = ?
                """;
        List<Transaction> rows = jdbcTemplate.query(sql, mapper, id, userId);
        return rows.stream().findFirst();
    }

    public List<Transaction> findAll(Long userId, String type, String category,
                                               LocalDate fromDate, LocalDate toDate) {
        StringBuilder sql = new StringBuilder("""
                SELECT id, user_id, type, amount, category, description,
                       transaction_date, created_at, updated_at
                FROM transactions WHERE user_id = ?
                """);
        List<Object> args = new ArrayList<>();
        args.add(userId);
        if (type != null && !type.isBlank()) {
            sql.append(" AND type = ?");
            args.add(type.toUpperCase());
        }
        if (category != null && !category.isBlank()) {
            sql.append(" AND LOWER(category) = LOWER(?)");
            args.add(category.trim());
        }
        if (fromDate != null) {
            sql.append(" AND transaction_date >= ?");
            args.add(fromDate);
        }
        if (toDate != null) {
            sql.append(" AND transaction_date <= ?");
            args.add(toDate);
        }
        sql.append(" ORDER BY transaction_date DESC, id DESC");
        return jdbcTemplate.query(sql.toString(), mapper, args.toArray());
    }

    public boolean update(Long id, Long userId, Transaction transaction) {
        String sql = """
                UPDATE transactions
                SET type = ?, amount = ?, category = ?, description = ?,
                    transaction_date = ?, updated_at = CURRENT_TIMESTAMP
                WHERE id = ? AND user_id = ?
                """;
        return jdbcTemplate.update(sql, transaction.getType(), transaction.getAmount(),
                transaction.getCategory(), transaction.getDescription(), transaction.getTransactionDate(),
                id, userId) > 0;
    }

    public boolean delete(Long id, Long userId) {
        return jdbcTemplate.update(
                "DELETE FROM transactions WHERE id = ? AND user_id = ?", id, userId) > 0;
    }

    public List<String> findCategories(Long userId) {
        return jdbcTemplate.queryForList(
                "SELECT DISTINCT category FROM transactions WHERE user_id = ? ORDER BY category",
                String.class, userId);
    }

    public List<CategoryTotal> categoryTotals(Long userId, String type,
                                               LocalDate fromDate, LocalDate toDate) {
        StringBuilder sql = new StringBuilder("""
                SELECT category, SUM(amount) AS total
                FROM transactions WHERE user_id = ?
                """);
        List<Object> args = new ArrayList<>();
        args.add(userId);
        if (type != null && !type.isBlank()) {
            sql.append(" AND type = ?");
            args.add(type.toUpperCase());
        }
        if (fromDate != null) {
            sql.append(" AND transaction_date >= ?");
            args.add(fromDate);
        }
        if (toDate != null) {
            sql.append(" AND transaction_date <= ?");
            args.add(toDate);
        }
        sql.append(" GROUP BY category ORDER BY category");
        return jdbcTemplate.query(sql.toString(), (rs, rowNum) ->
                new CategoryTotal(rs.getString("category"), rs.getBigDecimal("total")), args.toArray());
    }

    private final RowMapper<Transaction> mapper = this::mapRow;

    private Transaction mapRow(ResultSet rs, int rowNum) throws SQLException {
        String transactionType = rs.getString("type");
        Transaction item = "INCOME".equalsIgnoreCase(transactionType) ? new Income() : new Expense();
        item.setId(rs.getLong("id"));
        item.setUserId(rs.getLong("user_id"));
        item.setType(rs.getString("type"));
        item.setAmount(rs.getBigDecimal("amount"));
        item.setCategory(rs.getString("category"));
        item.setDescription(rs.getString("description"));
        item.setTransactionDate(rs.getObject("transaction_date", LocalDate.class));
        item.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        item.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        return item;
    }

    public record CategoryTotal(String category, BigDecimal total) {}
}
