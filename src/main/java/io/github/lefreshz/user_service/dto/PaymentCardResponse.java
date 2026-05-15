package io.github.lefreshz.user_service.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentCardResponse {
  private Long cardId;
  private String number;
  private String holder;
  private LocalDate expirationDate;
  private Boolean active;
  private Long userId;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
