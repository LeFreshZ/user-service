package io.github.lefreshz.user_service.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdatePaymentCardRequest {

  @NotBlank
  private String holder;
  @Future
  private LocalDate expirationDate;
}
