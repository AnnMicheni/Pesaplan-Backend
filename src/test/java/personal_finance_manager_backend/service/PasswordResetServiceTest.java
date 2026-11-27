package personal_finance_manager_backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import personal_finance_manager_backend.model.User;
import personal_finance_manager_backend.repository.PasswordResetTokenRepository;
import personal_finance_manager_backend.repository.UserRepository;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    @Mock private PasswordResetTokenRepository tokenRepository;
    @Mock private UserRepository userRepository;
    @Mock private UserService userService;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks private PasswordResetService passwordResetService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User("Test User", "test@example.com", "$2a$old", "KES");
        user.setId(7L);
    }

    @Test
    void createResetTokenReturnsRandomTokenAndStoresOnlyItsHash() {
        when(userService.findUserByEmail("test@example.com"))
                .thenReturn(Optional.of(user));

        String rawToken = passwordResetService.createResetToken(" TEST@example.com ");

        assertNotNull(rawToken);
        assertFalse(rawToken.isBlank());
        assertNotEquals(rawToken, sha256(rawToken));

        ArgumentCaptor<String> hashCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<LocalDateTime> expiryCaptor =
                ArgumentCaptor.forClass(LocalDateTime.class);

        verify(tokenRepository).invalidateUserTokens(7L);
        verify(tokenRepository).saveToken(
                eq(7L), hashCaptor.capture(), expiryCaptor.capture());

        assertEquals(sha256(rawToken), hashCaptor.getValue());
        assertEquals(64, hashCaptor.getValue().length());
        assertTrue(expiryCaptor.getValue().isAfter(LocalDateTime.now().plusMinutes(14)));
        assertTrue(expiryCaptor.getValue().isBefore(LocalDateTime.now().plusMinutes(16)));
    }

    @Test
    void createResetTokenReturnsNullForUnknownEmail() {
        when(userService.findUserByEmail("missing@example.com"))
                .thenReturn(Optional.empty());

        assertNull(passwordResetService.createResetToken("missing@example.com"));
        verifyNoInteractions(tokenRepository);
    }

    @Test
    void resetPasswordUpdatesPasswordAndConsumesToken() {
        String rawToken = "test-reset-token";
        String hash = sha256(rawToken);

        when(tokenRepository.findValidToken(hash)).thenReturn(Optional.of(7L));
        when(tokenRepository.markTokenAsUsed(hash)).thenReturn(true);
        when(passwordEncoder.encode("NewPassword123"))
                .thenReturn("$2a$new-password-hash");
        when(userRepository.updatePasswordHash(7L, "$2a$new-password-hash"))
                .thenReturn(true);

        assertDoesNotThrow(() ->
                passwordResetService.resetPassword(rawToken, "NewPassword123"));

        verify(tokenRepository).markTokenAsUsed(hash);
        verify(userRepository).updatePasswordHash(7L, "$2a$new-password-hash");
    }

    @Test
    void resetPasswordRejectsUnknownOrExpiredToken() {
        String hash = sha256("expired-token");
        when(tokenRepository.findValidToken(hash)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class,
                () -> passwordResetService.resetPassword(
                        "expired-token", "NewPassword123"));

        verify(tokenRepository, never()).markTokenAsUsed(anyString());
        verify(userRepository, never()).updatePasswordHash(anyLong(), anyString());
    }

    @Test
    void resetPasswordRejectsTokenAlreadyConsumedByAnotherRequest() {
        String hash = sha256("already-used-token");
        when(tokenRepository.findValidToken(hash)).thenReturn(Optional.of(7L));
        when(tokenRepository.markTokenAsUsed(hash)).thenReturn(false);

        assertThrows(ResponseStatusException.class,
                () -> passwordResetService.resetPassword(
                        "already-used-token", "NewPassword123"));

        verify(userRepository, never()).updatePasswordHash(anyLong(), anyString());
    }

    @Test
    void resetPasswordFailsIfUserCannotBeUpdated() {
        String rawToken = "valid-token";
        String hash = sha256(rawToken);
        when(tokenRepository.findValidToken(hash)).thenReturn(Optional.of(7L));
        when(tokenRepository.markTokenAsUsed(hash)).thenReturn(true);
        when(passwordEncoder.encode("NewPassword123")).thenReturn("$2a$encoded");
        when(userRepository.updatePasswordHash(7L, "$2a$encoded")).thenReturn(false);

        assertThrows(ResponseStatusException.class,
                () -> passwordResetService.resetPassword(rawToken, "NewPassword123"));
    }

    private static String sha256(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (Exception exception) {
            throw new IllegalStateException(exception);
        }
    }
}
