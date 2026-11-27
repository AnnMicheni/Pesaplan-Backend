package personal_finance_manager_backend.repository;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HexFormat;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class TokenRevocationRepository {
    private final JdbcTemplate jdbcTemplate;

    public TokenRevocationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void revoke(String token, Date expiresAt) {
        String sql = """
                INSERT INTO revoked_tokens (token_hash, expires_at)
                VALUES (?, ?)
                ON CONFLICT (token_hash) DO NOTHING
                """;
        jdbcTemplate.update(sql, hash(token), new Timestamp(expiresAt.getTime()));
    }

    public boolean isRevoked(String token) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM revoked_tokens WHERE token_hash = ? AND expires_at > CURRENT_TIMESTAMP",
                Integer.class, hash(token));
        return count != null && count > 0;
    }

    private String hash(String token) {
        try {
            byte[] bytes = MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable.", exception);
        }
    }
}
