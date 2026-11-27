package personal_finance_manager_backend.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import personal_finance_manager_backend.dto.ForgotPasswordRequest;
import personal_finance_manager_backend.dto.ResetPasswordRequest;
import personal_finance_manager_backend.model.User;
import personal_finance_manager_backend.security.JwtService;
import personal_finance_manager_backend.service.PasswordResetService;
import personal_finance_manager_backend.service.UserService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock private UserService userService;
    @Mock private JwtService jwtService;
    @Mock private PasswordResetService passwordResetService;

    @InjectMocks private AuthController authController;

    @Test
    void forgotPasswordReturnsTokenForExistingAccount() {
        when(passwordResetService.createResetToken("test@example.com"))
                .thenReturn("temporary-reset-token");

        var response = authController.forgotPassword(
                new ForgotPasswordRequest("test@example.com"));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("temporary-reset-token", response.getBody().get("resetToken"));
    }

    @Test
    void forgotPasswordDoesNotRevealWhetherUnknownAccountExists() {
        when(passwordResetService.createResetToken("missing@example.com"))
                .thenReturn(null);

        var response = authController.forgotPassword(
                new ForgotPasswordRequest("missing@example.com"));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(response.getBody().containsKey("resetToken"));
        assertTrue(response.getBody().get("message").contains("If the account exists"));
    }

    @Test
    void resetPasswordDelegatesTokenAndNewPasswordToService() {
        var response = authController.resetPassword(
                new ResetPasswordRequest("valid-token", "NewPassword123"));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Password reset successfully.", response.getBody().get("message"));
        verify(passwordResetService).resetPassword("valid-token", "NewPassword123");
    }
}
