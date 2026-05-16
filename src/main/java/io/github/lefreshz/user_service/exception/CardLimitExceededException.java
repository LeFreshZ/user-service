package io.github.lefreshz.user_service.exception;

public class CardLimitExceededException extends RuntimeException {

  public CardLimitExceededException(String message) {
    super(message);
  }
}
