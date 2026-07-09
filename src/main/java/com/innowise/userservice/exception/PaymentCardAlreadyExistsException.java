package com.innowise.userservice.exception;

public class PaymentCardAlreadyExistsException extends RuntimeException {

  public PaymentCardAlreadyExistsException(String message) {
    super(message);
  }
}
