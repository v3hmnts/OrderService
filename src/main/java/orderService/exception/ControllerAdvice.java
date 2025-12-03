package orderService.exception;

import orderService.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ControllerAdvice {

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                Instant.now(),
                "Access denied",
                HttpStatus.FORBIDDEN,
                null
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(TokenValidationException.class)
    public ResponseEntity<ErrorResponse> handleTokenValidationException(TokenValidationException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                Instant.now(),
                ex.getMessage(),
                HttpStatus.UNAUTHORIZED,
                null
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler({ItemNotFoundException.class, OrderNotFoundException.class, UserNotFoundException.class})
    public ResponseEntity<ErrorResponse> handleBaseException(RuntimeException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                Instant.now(),
                ex.getMessage(),
                HttpStatus.NOT_FOUND,
                null
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }


    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorResponse> handleHandlerMethodValidationException(HandlerMethodValidationException ex) {

        List<String> errors = new ArrayList<>();

        ex.getParameterValidationResults().forEach(parameterResult -> {
            String parameterName = parameterResult.getMethodParameter().getParameterName();

            parameterResult.getResolvableErrors().forEach(error -> {
                errors.add(String.format("%s:%s", parameterName, error.getDefaultMessage()));
            });
        });

        ErrorResponse errorResponse = new ErrorResponse(
                Instant.now(),
                "Validation failed",
                HttpStatus.BAD_REQUEST,
                errors
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({MethodArgumentNotValidException.class})
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {

        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.toList());

        ErrorResponse errorResponse = new ErrorResponse(
                Instant.now(),
                "Validation failed",
                HttpStatus.BAD_REQUEST,
                errors
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        ErrorResponse errorResponse = new ErrorResponse(Instant.now(), ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, null);
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
