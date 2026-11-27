
package personal_finance_manager_backend.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import personal_finance_manager_backend.dto.AuthResponse;
import personal_finance_manager_backend.dto.ForgotPasswordRequest;
import personal_finance_manager_backend.dto.LoginRequest;
import personal_finance_manager_backend.dto.RegisterRequest;
import personal_finance_manager_backend.dto.ResetPasswordRequest;
import personal_finance_manager_backend.model.User;
import personal_finance_manager_backend.security.JwtService;
import personal_finance_manager_backend.service.PasswordResetService;
import personal_finance_manager_backend.service.UserService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService;
    private final PasswordResetService passwordResetService;

    public AuthController(
            UserService userService,
            JwtService jwtService,
            PasswordResetService passwordResetService) {

        this.userService = userService;
        this.jwtService = jwtService;
        this.passwordResetService = passwordResetService;
    }

    // Register a new user
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request) {

        User user = new User(
                request.getFullName(),
                request.getEmail(),
                request.getPassword(),
                request.getCurrencyPreference()
        );

        User registeredUser = userService.registerUser(user);

        String token = jwtService.generateToken(
                registeredUser.getEmail()
        );

        AuthResponse response = new AuthResponse(
                "Account created successfully",
                token,
                "Bearer",
                registeredUser.getId(),
                registeredUser.getFullName(),
                registeredUser.getEmail(),
                registeredUser.getCurrencyPreference(),
                registeredUser.getCreatedAt()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Log in an existing user
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request) {

        User user = userService.loginUser(
                request.getEmail(),
                request.getPassword()
        );

        String token = jwtService.generateToken(user.getEmail());

        AuthResponse response = new AuthResponse(
                "Login successful",
                token,
                "Bearer",
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getCurrencyPreference(),
                user.getCreatedAt()
        );

        return ResponseEntity.ok(response);
    }

    // Request a password reset
    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {

        String token = passwordResetService.createResetToken(
                request.email()
        );

        if (token == null) {
            return ResponseEntity.ok(Map.of(
                    "message",
                    "If the account exists, a password reset can be completed."
            ));
        }

        return ResponseEntity.ok(Map.of(
                "message",
                "Password reset token generated successfully.",
                "resetToken",
                token
        ));
    }

    // Reset password using a valid token
    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {

        passwordResetService.resetPassword(
                request.token(),
                request.newPassword()
        );

        return ResponseEntity.ok(Map.of(
                "message",
                "Password reset successfully."
        ));
    }
}
