/* Copyright 2026 UnitedCloud. All rights reserved. */
package uc.orchestrator.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;

@Value
@AllArgsConstructor
@Builder(toBuilder = true)
public class StreamerRegistrationRequestDTO {

  @NotBlank(message = "Streamer name must not be blank")
  @Size(max = 20, message = "Streamer name must not exceed 20 characters")
  String streamerName;

  @Positive(message = "Port must be greater than 0") Integer port;

  @NotBlank(message = "Root VOD path must not be blank")
  @Size(max = 255, message = "Root VOD path must not exceed 255 characters")
  String rootVodPath;

  @Positive(message = "Capacity must be greater than 0") Integer capacity;
}
