package com.innowise.userservice.exception.handler;

import com.innowise.userservice.dto.ErrorResponse;
import com.innowise.userservice.exception.CardLimitExceededException;
import com.innowise.userservice.exception.PaymentCardAlreadyExistsException;
import com.innowise.userservice.exception.PaymentCardNotFoundException;
import com.innowise.userservice.exception.UserAlreadyExistsException;
import com.innowise.userservice.exception.UserNotFoundException;
import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import javax.naming.AuthenticationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler({
      UserNotFoundException.class,
      PaymentCardNotFoundException.class
  })
  public ResponseEntity<ErrorResponse> handleNotFound(RuntimeException ex) {
    ErrorResponse response = new ErrorResponse(LocalDateTime.now(), 404, ex.getMessage());

    return ResponseEntity.status(404).body(response);
  }

  @ExceptionHandler({
      UserAlreadyExistsException.class,
      PaymentCardAlreadyExistsException.class,
      CardLimitExceededException.class
  })
  public ResponseEntity<ErrorResponse> handleConflict(RuntimeException ex) {
    ErrorResponse response = new ErrorResponse(LocalDateTime.now(), 409, ex.getMessage());

    return ResponseEntity.status(409).body(response);
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
    ErrorResponse response = new ErrorResponse(LocalDateTime.now(), 403, "Access Denied");

    return ResponseEntity.status(403).body(response);
  }

  @ExceptionHandler(AuthenticationException.class)
  public ResponseEntity<ErrorResponse> handleAuthentication(AuthenticationException ex) {
    ErrorResponse response = new ErrorResponse(LocalDateTime.now(), 401, "Unauthorized");

    return ResponseEntity.status(401).body(response);
  }
}
