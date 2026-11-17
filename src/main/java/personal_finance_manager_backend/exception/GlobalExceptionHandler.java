
package personal_finance_manager_backend.exception;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Duplicate email
    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicateEmail(
            EmailAlreadyExistsException exception) {

        return errorResponse(
                HttpStatus.CONFLICT,
                exception.getMessage()
        );
    }

    // Incorrect login details
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidCredentials(
            InvalidCredentialsException exception) {

        return errorResponse(
                HttpStatus.UNAUTHORIZED,
                exception.getMessage()
        );
    }

    // Invalid request fields
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(
            MethodArgumentNotValidException exception) {

        Map<String, String> fieldErrors = new LinkedHashMap<>();

        exception.getBindingResult().getFieldErrors().forEach(error ->
                fieldErrors.putIfAbsent(
                        error.getField(),
                        error.getDefaultMessage()
                )
        );

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", "Validation failed");
        response.put("errors", fieldErrors);

        return ResponseEntity.badRequest().body(response);
    }

    // Invalid JSON
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidJson(
            HttpMessageNotReadableException exception) {

        return errorResponse(
                HttpStatus.BAD_REQUEST,
                "Invalid request body."
        );
    }

    // Database constraint errors
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDatabaseConflict(
            DataIntegrityViolationException exception) {

        return errorResponse(
                HttpStatus.CONFLICT,
                "The request conflicts with existing database data."
        );
    }

    // Unexpected errors
    @ExceptionHandler(Exception.class)
public ResponseEntity<Map<String, Object>> handleGeneralErrors(
        Exception exception) {

    exception.printStackTrace();

    return errorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "An unexpected error occurred."
    );
}

    private ResponseEntity<Map<String, Object>> errorResponse(
            HttpStatus status,
            String message) {

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", message);

        return ResponseEntity.status(status).body(response);
    }

    @ExceptionHandler(NoResourceFoundException.class)
public ResponseEntity<Map<String, Object>> handleNotFound(
        NoResourceFoundException exception) {

    return errorResponse(
            HttpStatus.NOT_FOUND,
            "The requested endpoint does not exist."
    );
}
}
