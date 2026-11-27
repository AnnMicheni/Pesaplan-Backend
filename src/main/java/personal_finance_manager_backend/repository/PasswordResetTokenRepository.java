
package personal_finance_manager_backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class PasswordResetTokenRepository {

    private final JdbcTemplate jdbcTemplate;

    public PasswordResetTokenRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void invalidateUserTokens(Long userId) {
        String sql = """
                UPDATE password_reset_tokens
                SET used_at = CURRENT_TIMESTAMP
                WHERE user_id = ?
                AND used_at IS NULL
                """;

        jdbcTemplate.update(sql, userId);
    }

    public void saveToken(
            Long userId,
            String tokenHash,
            java.time.LocalDateTime expiresAt) {

        String sql = """
                INSERT INTO password_reset_tokens
                (user_id, token_hash, expires_at)
                VALUES (?, ?, ?)
                """;

        jdbcTemplate.update(sql, userId, tokenHash, expiresAt);
    }

    public Optional<Long> findValidToken(String tokenHash) {
        String sql = """
                SELECT user_id
                FROM password_reset_tokens
                WHERE token_hash = ?
                AND used_at IS NULL
                AND expires_at > CURRENT_TIMESTAMP
                """;

        List<Long> userIds = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> rs.getLong("user_id"),
                tokenHash
        );

        return userIds.stream().findFirst();
    }

    public boolean markTokenAsUsed(String tokenHash) {
        String sql = """
                UPDATE password_reset_tokens
                SET used_at = CURRENT_TIMESTAMP
                WHERE token_hash = ?
                AND used_at IS NULL
                AND expires_at > CURRENT_TIMESTAMP
                """;

        return jdbcTemplate.update(sql, tokenHash) > 0;
    }
}
