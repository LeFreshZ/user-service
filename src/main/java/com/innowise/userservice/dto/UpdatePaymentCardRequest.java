package com.innowise.userservice.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdatePaymentCardRequest {

  @NotBlank
  private String holder;
  @NotNull
  @Future
  private LocalDate expirationDate;
}
