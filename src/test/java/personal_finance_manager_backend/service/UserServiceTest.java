package personal_finance_manager_backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import personal_finance_manager_backend.exception.EmailAlreadyExistsException;
import personal_finance_manager_backend.exception.InvalidCredentialsException;
import personal_finance_manager_backend.model.User;
import personal_finance_manager_backend.repository.UserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks private UserService userService;

    private User activeUser;

    @BeforeEach
    void setUp() {
        activeUser = new User("Test User", "test@example.com",
                "$2a$hashed-password", "KES");
        activeUser.setId(10L);
        activeUser.setRole("USER");
        activeUser.setActive(true);
    }

    @Test
    void registerUserNormalizesEmailHashesPasswordAndDefaultsCurrency() {
        User input = new User("Test User", "  TEST@Example.com  ",
                "PlainPassword123", null);

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.empty());
        when(passwordEncoder.encode("PlainPassword123"))
                .thenReturn("$2a$encoded");
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        User saved = userService.registerUser(input);

        assertEquals("test@example.com", saved.getEmail());
        assertEquals("$2a$encoded", saved.getPasswordHash());
        assertEquals("KES", saved.getCurrencyPreference());
        verify(userRepository).save(input);
    }

    @Test
    void registerUserRejectsDuplicateEmail() {
        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(activeUser));

        assertThrows(EmailAlreadyExistsException.class,
                () -> userService.registerUser(new User(
                        "Another User", "TEST@example.com",
                        "Password123", "KES")));

        verify(userRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void registerUserConvertsDatabaseDuplicateToConflictException() {
        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.empty());
        when(passwordEncoder.encode("Password123"))
                .thenReturn("$2a$encoded");
        when(userRepository.save(any(User.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate"));

        assertThrows(EmailAlreadyExistsException.class,
                () -> userService.registerUser(new User(
                        "Test User", "test@example.com",
                        "Password123", "KES")));
    }

    @Test
    void loginReturnsActiveUserWhenPasswordMatches() {
        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches("Password123", "$2a$hashed-password"))
                .thenReturn(true);

        User result = userService.loginUser(" TEST@example.com ", "Password123");

        assertSame(activeUser, result);
    }

    @Test
    void loginRejectsUnknownEmail() {
        when(userRepository.findByEmail("missing@example.com"))
                .thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class,
                () -> userService.loginUser("missing@example.com", "Password123"));
    }

    @Test
    void loginRejectsIncorrectPassword() {
        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches("WrongPassword", "$2a$hashed-password"))
                .thenReturn(false);

        assertThrows(InvalidCredentialsException.class,
                () -> userService.loginUser("test@example.com", "WrongPassword"));
    }

    @Test
    void loginRejectsSuspendedAccountBeforeCheckingPassword() {
        activeUser.setActive(false);
        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(activeUser));

        InvalidCredentialsException exception = assertThrows(
                InvalidCredentialsException.class,
                () -> userService.loginUser("test@example.com", "Password123"));

        assertEquals("This account has been suspended.", exception.getMessage());
        verify(passwordEncoder, never()).matches(any(), any());
    }

    @Test
    void updateAccountStatusThrowsNotFoundWhenUserDoesNotExist() {
        when(userRepository.updateAccountStatus(999L, false))
                .thenReturn(false);

        assertThrows(org.springframework.web.server.ResponseStatusException.class,
                () -> userService.updateAccountStatus(999L, false));
    }
}
