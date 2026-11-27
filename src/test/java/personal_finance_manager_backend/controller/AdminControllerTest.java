package personal_finance_manager_backend.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import personal_finance_manager_backend.model.User;
import personal_finance_manager_backend.service.UserService;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    @Mock private UserService userService;
    @InjectMocks private AdminController adminController;

    @Test
    void getAllUsersReturnsUserSummariesWithoutPasswordHashes() {
        User user = new User("Test User", "test@example.com", "SECRET_HASH", "KES");
        user.setId(1L);
        user.setRole("USER");
        user.setActive(true);
        user.setCreatedAt(LocalDateTime.now());

        when(userService.findAllUsers()).thenReturn(List.of(user));

        var response = adminController.getAllUsers();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("test@example.com", response.getBody().get(0).email());
        assertTrue(response.getBody().get(0).active());
        assertFalse(response.getBody().get(0).toString().contains("SECRET_HASH"));
    }

    @Test
    void suspendUserDelegatesInactiveStatus() {
        var response = adminController.suspendUser(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Account suspended successfully.",
                response.getBody().get("message"));
        verify(userService).updateAccountStatus(1L, false);
    }

    @Test
    void activateUserDelegatesActiveStatus() {
        var response = adminController.activateUser(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Account activated successfully.",
                response.getBody().get("message"));
        verify(userService).updateAccountStatus(1L, true);
    }
}
