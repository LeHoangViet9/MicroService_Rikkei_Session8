package rikkei.edu.enpointmentservice.exception;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import rikkei.edu.enpointmentservice.dto.response.ApiResponseError;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalHandleException {

    @ExceptionHandler(ServiceUnavailableException.class)
    public ResponseEntity<ApiResponseError> handleServiceUnavailable(ServiceUnavailableException ex) {
        ApiResponseError errorResponse = ApiResponseError.builder()
                .status(HttpStatus.SERVICE_UNAVAILABLE.value()) // Status 503
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorResponse);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponseError> handleBadRequest(IllegalArgumentException ex) {
        ApiResponseError errorResponse = ApiResponseError.builder()
                .status(HttpStatus.BAD_REQUEST.value()) // Status 400
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
}
