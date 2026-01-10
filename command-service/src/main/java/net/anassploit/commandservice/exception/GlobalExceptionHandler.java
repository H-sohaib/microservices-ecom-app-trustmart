package net.anassploit.commandservice.exception;

import feign.FeignException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice(basePackages = "net.anassploit.commandservice.controller")
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final Logger errorLog = LoggerFactory.getLogger("ERROR_LOG");

    private String getUserContext(HttpServletRequest request) {
        String userId = request.getHeader("X-User-Id");
        String username = request.getHeader("X-User-Name");
        String traceId = MDC.get("traceId");
        return String.format("traceId=%s, userId=%s, username=%s",
                traceId != null ? traceId : "N/A",
                userId != null ? userId : "anonymous",
                username != null ? username : "anonymous");
    }

    @ExceptionHandler(CommandNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCommandNotFoundException(
            CommandNotFoundException ex, HttpServletRequest request) {
        log.error("APPLICATION_ERROR | type=CommandNotFound | path={} | message={} | context=[{}]",
                request.getRequestURI(), ex.getMessage(), getUserContext(request));
        errorLog.error("CommandNotFoundException: {} | {}", ex.getMessage(), getUserContext(request));
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error("Not Found")
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleProductNotFoundException(
            ProductNotFoundException ex, HttpServletRequest request) {
        log.error("APPLICATION_ERROR | type=ProductNotFound | path={} | message={} | context=[{}]",
                request.getRequestURI(), ex.getMessage(), getUserContext(request));
        errorLog.error("ProductNotFoundException: {} | {}", ex.getMessage(), getUserContext(request));
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error("Not Found")
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientStockException(
            InsufficientStockException ex, HttpServletRequest request) {
        log.error("APPLICATION_ERROR | type=InsufficientStock | path={} | message={} | context=[{}]",
                request.getRequestURI(), ex.getMessage(), getUserContext(request));
        errorLog.error("InsufficientStockException: {} | {}", ex.getMessage(), getUserContext(request));
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Bad Request")
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidCommandStatusException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCommandStatusException(
            InvalidCommandStatusException ex, HttpServletRequest request) {
        log.error("APPLICATION_ERROR | type=InvalidCommandStatus | path={} | message={} | context=[{}]",
                request.getRequestURI(), ex.getMessage(), getUserContext(request));
        errorLog.error("InvalidCommandStatusException: {} | {}", ex.getMessage(), getUserContext(request));
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Bad Request")
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(FeignException.class)
    public ResponseEntity<ErrorResponse> handleFeignException(
            FeignException ex, HttpServletRequest request) {
        log.error("APPLICATION_ERROR | type=FeignException | path={} | message={} | context=[{}]",
                request.getRequestURI(), ex.getMessage(), getUserContext(request));
        errorLog.error("FeignException: {} | {}", ex.getMessage(), getUserContext(request));
        HttpStatus status = HttpStatus.valueOf(ex.status() > 0 ? ex.status() : 500);
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message("Error communicating with Product Service: " + ex.getMessage())
                .path(request.getRequestURI())
                .build();
        return new ResponseEntity<>(errorResponse, status);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        log.error("APPLICATION_ERROR | type=ValidationError | path={} | context=[{}]",
                request.getRequestURI(), getUserContext(request));
        Map<String, String> validationErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            validationErrors.put(fieldName, errorMessage);
        });
        errorLog.error("ValidationError: fields={} | {}", validationErrors.keySet(), getUserContext(request));

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Validation Failed")
                .message("Input validation failed")
                .path(request.getRequestURI())
                .validationErrors(validationErrors)
                .build();
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex, HttpServletRequest request) {
        log.error("APPLICATION_ERROR | type=UnexpectedError | path={} | message={} | context=[{}]",
                request.getRequestURI(), ex.getMessage(), getUserContext(request), ex);
        errorLog.error("UnexpectedError: {} | {} | stackTrace available in main log",
                ex.getMessage(), getUserContext(request));
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Internal Server Error")
                .message("An unexpected error occurred")
                .path(request.getRequestURI())
                .build();
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
