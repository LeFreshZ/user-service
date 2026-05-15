package io.github.lefreshz.user_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserRequest {

  @NotBlank
  private String name;
  @NotBlank
  private String surname;
  @Past
  private LocalDate birthDate;
  @Email
  @NotBlank
  private String email;
}
