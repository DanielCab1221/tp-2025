package edu.utn.frsf.isi.dan.reservas_svc.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

@RestControllerAdvice
public class ControllerAdvisor {

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ExceptionInfo> handleMethodNotSupportedException(HttpRequestMethodNotSupportedException ex, WebRequest request) {
        return build(ex.getMessage(), request, HttpStatus.METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ExceptionInfo> handleIllegalArgumentException(IllegalArgumentException ex, WebRequest request) {
        return build(ex.getMessage(), request, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ExceptionInfo> handleValidationException(MethodArgumentNotValidException ex, WebRequest request) {
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .reduce((message1, message2) -> message1 + ", " + message2)
                .orElse("Validation error");
        return build(errorMessage, request, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionInfo> handleGeneralException(Exception ex, WebRequest request) {
        return build(ex.getMessage(), request, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<ExceptionInfo> build(String message, WebRequest request, HttpStatus status) {
        ExceptionInfo exceptionInfo = new ExceptionInfo(
                message,
                request.getDescription(false),
                String.valueOf(System.currentTimeMillis()),
                status.value()
        );
        return new ResponseEntity<>(exceptionInfo, status);
    }
}
