
package personal_finance_manager_backend.service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import personal_finance_manager_backend.model.User;
import personal_finance_manager_backend.repository.PasswordResetTokenRepository;
import personal_finance_manager_backend.repository.UserRepository;

@Service
public class PasswordResetService {

    private final PasswordResetTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom = new SecureRandom();

    public PasswordResetService(
            PasswordResetTokenRepository tokenRepository,
            UserRepository userRepository,
            UserService userService,
            PasswordEncoder passwordEncoder) {

        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public String createResetToken(String email) {

        User user = userService.findUserByEmail(
                email.trim().toLowerCase()
        ).orElse(null);

        if (user == null) {
            return null;
        }

        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);

        String token = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(randomBytes);

        String tokenHash = hashToken(token);

        tokenRepository.invalidateUserTokens(user.getId());

        tokenRepository.saveToken(
                user.getId(),
                tokenHash,
                LocalDateTime.now().plusMinutes(15)
        );

        return token;
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {

        String tokenHash = hashToken(token);

        Long userId = tokenRepository.findValidToken(tokenHash)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Reset token is invalid or expired."
                ));

        if (!tokenRepository.markTokenAsUsed(tokenHash)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Reset token is invalid or expired."
            );
        }

        String hashedPassword = passwordEncoder.encode(newPassword);

        boolean updated = userRepository.updatePasswordHash(
                userId,
                hashedPassword
        );

        if (!updated) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "User not found."
            );
        }
    }

    private String hashToken(String token) {

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(
                    token.getBytes(java.nio.charset.StandardCharsets.UTF_8)
            );

            return HexFormat.of().formatHex(hash);

        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(
                    "Unable to hash reset token.",
                    exception
            );
        }
    }
}
