package com.innowise.userservice.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ErrorResponse {
  private LocalDateTime time;
  private int status;
  private String message;
}
