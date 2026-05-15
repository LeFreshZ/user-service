package io.github.lefreshz.user_service.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserResponse {
  private Long userId;
  private String name;
  private String surname;
  private LocalDate birthDate;
  private String email;
  private Boolean active;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
