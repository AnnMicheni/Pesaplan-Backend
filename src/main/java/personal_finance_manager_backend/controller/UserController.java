package personal_finance_manager_backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import personal_finance_manager_backend.dto.UserProfileResponse;
import personal_finance_manager_backend.model.User;
import personal_finance_manager_backend.service.UserService;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getCurrentUser(
            Authentication authentication) {

        String email = authentication.getName();

        User user = userService.findUserByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found.")
                );

        UserProfileResponse response = new UserProfileResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getCurrencyPreference(),
                user.getCreatedAt()
        );

        return ResponseEntity.ok(response);
    }
}