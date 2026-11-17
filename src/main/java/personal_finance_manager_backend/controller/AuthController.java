
package personal_finance_manager_backend.controller;

import personal_finance_manager_backend.dto.AuthResponse;
import personal_finance_manager_backend.dto.LoginRequest;
import personal_finance_manager_backend.dto.RegisterRequest;
import personal_finance_manager_backend.model.User;
import personal_finance_manager_backend.security.JwtService;
import personal_finance_manager_backend.service.UserService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService;

    public AuthController(
            UserService userService,
            JwtService jwtService) {

        this.userService = userService;
        this.jwtService = jwtService;
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
}
