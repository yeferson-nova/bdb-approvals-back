package co.com.bancodebogota.bdbapprovals.infrastructure.rest;

import co.com.bancodebogota.bdbapprovals.domain.exception.DomainException;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

@ControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<Map<String,Object>> handleDomain(DomainException ex) {
        return ResponseEntity.badRequest().body(Map.of(
                "timestamp", Instant.now().toString(),
                "error", "domain_error",
                "message", ex.getMessage()
        ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String,Object>> handleValidation(MethodArgumentNotValidException ex) {
        return ResponseEntity.badRequest().body(Map.of(
                "timestamp", Instant.now().toString(),
                "error", "validation_error",
                "message", ex.getMessage()
        ));
    }
}
