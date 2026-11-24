
package personal_finance_manager_backend.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import personal_finance_manager_backend.dto.AdminUserResponse;
import personal_finance_manager_backend.service.UserService;

@RestController
@RequestMapping("/api/admin/users")
public class AdminController {

    private final UserService userService;

    public AdminController(UserService userService) {
        this.userService = userService;
    }

    // View all registered users
    @GetMapping
    public ResponseEntity<List<AdminUserResponse>> getAllUsers() {

        List<AdminUserResponse> users =
                userService.findAllUsers()
                        .stream()
                        .map(user -> new AdminUserResponse(
                                user.getId(),
                                user.getFullName(),
                                user.getEmail(),
                                user.getCurrencyPreference(),
                                user.getRole(),
                                user.isActive(),
                                user.getCreatedAt()
                        ))
                        .toList();

        return ResponseEntity.ok(users);
    }

    // Suspend an account
    @PatchMapping("/{id}/suspend")
    public ResponseEntity<Map<String, String>> suspendUser(
            @PathVariable Long id) {

        userService.updateAccountStatus(id, false);

        return ResponseEntity.ok(
                Map.of("message", "Account suspended successfully.")
        );
    }

    // Activate an account
    @PatchMapping("/{id}/activate")
    public ResponseEntity<Map<String, String>> activateUser(
            @PathVariable Long id) {

        userService.updateAccountStatus(id, true);

        return ResponseEntity.ok(
                Map.of("message", "Account activated successfully.")
        );
    }
}
