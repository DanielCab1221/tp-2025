package edu.utn.frsf.isi.dan.gestion.exception;

import jakarta.persistence.EntityNotFoundException;
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
  public ResponseEntity<ExceptionInfo> handleMethodNotSupportedException(
      HttpRequestMethodNotSupportedException ex, WebRequest request) {
    ExceptionInfo exceptionInfo =
        new ExceptionInfo(
            ex.getMessage(),
            request.getDescription(false),
            String.valueOf(System.currentTimeMillis()),
            HttpStatus.METHOD_NOT_ALLOWED.value());
    return new ResponseEntity<>(exceptionInfo, HttpStatus.METHOD_NOT_ALLOWED);
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ExceptionInfo> handleIllegalArgumentException(
      IllegalArgumentException ex, WebRequest request) {
    ExceptionInfo exceptionInfo =
        new ExceptionInfo(
            ex.getMessage(),
            request.getDescription(false),
            String.valueOf(System.currentTimeMillis()),
            HttpStatus.BAD_REQUEST.value());
    return new ResponseEntity<>(exceptionInfo, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(EntityNotFoundException.class)
  public ResponseEntity<ExceptionInfo> handleEntityNotFoundException(
      EntityNotFoundException ex, WebRequest request) {
    ExceptionInfo exceptionInfo =
        new ExceptionInfo(
            ex.getMessage(),
            request.getDescription(false),
            String.valueOf(System.currentTimeMillis()),
            HttpStatus.NOT_FOUND.value());
    return new ResponseEntity<>(exceptionInfo, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ExceptionInfo> handleValidationException(
      MethodArgumentNotValidException ex, WebRequest request) {
    String errorMessage =
        ex.getBindingResult().getFieldErrors().stream()
            .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
            .reduce((message1, message2) -> message1 + ", " + message2)
            .orElse("Validation error");

    ExceptionInfo exceptionInfo =
        new ExceptionInfo(
            errorMessage,
            request.getDescription(false),
            String.valueOf(System.currentTimeMillis()),
            HttpStatus.BAD_REQUEST.value());
    return new ResponseEntity<>(exceptionInfo, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ExceptionInfo> handleGeneralException(Exception ex, WebRequest request) {
    ExceptionInfo exceptionInfo =
        new ExceptionInfo(
            ex.getMessage(),
            request.getDescription(false),
            String.valueOf(System.currentTimeMillis()),
            HttpStatus.INTERNAL_SERVER_ERROR.value());
    return new ResponseEntity<>(exceptionInfo, HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
