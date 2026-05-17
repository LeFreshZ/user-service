package io.github.lefreshz.user_service.exception;

public class PaymentCardAlreadyExistsException extends RuntimeException {

  public PaymentCardAlreadyExistsException(String message) {
    super(message);
  }
}
