package io.github.lefreshz.user_service.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreatePaymentCardRequest {

  @NotBlank
  @Pattern(regexp = "^\\d{16}$")
  private String number;
  @NotBlank
  private String holder;
  @Future
  private LocalDate expirationDate;
  @NotNull
  private Long userId;
}
