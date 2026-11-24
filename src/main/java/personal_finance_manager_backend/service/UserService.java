
package personal_finance_manager_backend.service;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import personal_finance_manager_backend.exception.EmailAlreadyExistsException;
import personal_finance_manager_backend.exception.InvalidCredentialsException;
import personal_finance_manager_backend.model.User;
import personal_finance_manager_backend.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User registerUser(User user) {

        String email = user.getEmail().trim().toLowerCase(Locale.ROOT);

        if (userRepository.findByEmail(email).isPresent()) {
            throw new EmailAlreadyExistsException(
                    "An account with this email already exists."
            );
        }

        user.setEmail(email);

        String hashedPassword =
                passwordEncoder.encode(user.getPasswordHash());

        user.setPasswordHash(hashedPassword);

        if (user.getCurrencyPreference() == null ||
                user.getCurrencyPreference().isBlank()) {
            user.setCurrencyPreference("KES");
        }

        try {
            return userRepository.save(user);
        } catch (DataIntegrityViolationException exception) {
            throw new EmailAlreadyExistsException(
                    "An account with this email already exists."
            );
        }
    }

    public User loginUser(String email, String password) {

        String normalizedEmail =
                email.trim().toLowerCase(Locale.ROOT);

        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() ->
                        new InvalidCredentialsException(
                                "Invalid email or password."
                        )
                );

        if (!user.isActive()) {
            throw new InvalidCredentialsException(
                    "This account has been suspended."
            );
        }

        if (!passwordEncoder.matches(
                password,
                user.getPasswordHash())) {

            throw new InvalidCredentialsException(
                    "Invalid email or password."
            );
        }

        return user;
    }

    public Optional<User> findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // Retrieve all users for administration
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    // Suspend or activate a user
    public void updateAccountStatus(Long id, boolean active) {

        boolean updated =
                userRepository.updateAccountStatus(id, active);

        if (!updated) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "User not found."
            );
        }
    }
}
