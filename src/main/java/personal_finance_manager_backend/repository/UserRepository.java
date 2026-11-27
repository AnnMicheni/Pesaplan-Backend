
package personal_finance_manager_backend.repository;

import java.sql.ResultSet;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import personal_finance_manager_backend.model.User;

@Repository
public class UserRepository {

    private final JdbcTemplate jdbcTemplate;

    public UserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // Save a new user
    public User save(User user) {

        String sql = """
                INSERT INTO users
                (full_name, email, password_hash, currency_preference)
                VALUES (?, ?, ?, ?)
                RETURNING id, full_name, email, password_hash,
                          currency_preference, role, is_active,
                          created_at, updated_at
                """;

        return jdbcTemplate.queryForObject(
                sql,
                userRowMapper,
                user.getFullName(),
                user.getEmail(),
                user.getPasswordHash(),
                user.getCurrencyPreference()
        );
    }

    // Find a user using email
    public Optional<User> findByEmail(String email) {

        String sql = """
                SELECT id, full_name, email, password_hash,
                       currency_preference, role, is_active,
                       created_at, updated_at
                FROM users
                WHERE email = ?
                """;

        List<User> users = jdbcTemplate.query(
                sql,
                userRowMapper,
                email
        );

        if (users.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(users.get(0));
    }

    // Retrieve all users
    public List<User> findAll() {

        String sql = """
                SELECT id, full_name, email, password_hash,
                       currency_preference, role, is_active,
                       created_at, updated_at
                FROM users
                ORDER BY id
                """;

        return jdbcTemplate.query(sql, userRowMapper);
    }

    // Suspend or activate an account
    public boolean updateAccountStatus(Long id, boolean active) {

        String sql = """
                UPDATE users
                SET is_active = ?, updated_at = CURRENT_TIMESTAMP
                WHERE id = ?
                """;

        return jdbcTemplate.update(sql, active, id) > 0;
    }

    // Convert database rows into User objects
    private final RowMapper<User> userRowMapper = (ResultSet rs, int rowNum) -> {

        User user = new User();

        user.setId(rs.getLong("id"));
        user.setFullName(rs.getString("full_name"));
        user.setEmail(rs.getString("email"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setCurrencyPreference(rs.getString("currency_preference"));
        user.setRole(rs.getString("role"));
        user.setActive(rs.getBoolean("is_active"));
        user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        user.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());

        return user;
    };

    public boolean updatePasswordHash(Long userId, String passwordHash) {
    String sql = """
            UPDATE users
            SET password_hash = ?, updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
            """;

    return jdbcTemplate.update(sql, passwordHash, userId) > 0;
}
}

