package io.github.lefreshz.user_service.exception;

public class PaymentCardNotFoundException extends RuntimeException {

  public PaymentCardNotFoundException(String message) {
    super(message);
  }
}
