
package personal_finance_manager_backend.service;

import java.util.Locale;
import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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

    // Register a new user
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

    // Log in an existing user
    public User loginUser(String email, String password) {

        String normalizedEmail =
                email.trim().toLowerCase(Locale.ROOT);

        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() ->
                        new InvalidCredentialsException(
                                "Invalid email or password."
                        )
                );

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
}
