/* Copyright 2026 UnitedCloud. All rights reserved. */
package uc.orchestrator.dto;

import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
@AllArgsConstructor
public class StreamerUpdateRequestDTO {

  String streamerName;

  @Positive(message = "Port must be greater than 0") Integer port;

  String rootVodPath;

  @Positive(message = "Capacity must be greater than 0") Integer capacity;
}
