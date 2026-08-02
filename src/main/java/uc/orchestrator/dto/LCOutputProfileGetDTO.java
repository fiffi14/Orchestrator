/* Copyright 2026 UnitedCloud. All rights reserved. */
package uc.orchestrator.dto;

import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
@AllArgsConstructor
public class LCOutputProfileGetDTO {
  Long id;
  String configName;
  String videoCodec;
  String audioCodec;

  @Positive(message = "Width needs to be a positive value.") Integer width;

  @Positive(message = "Height needs to be a positive value.") Integer height;

  @Positive(message = "Bitrate needs to be a positive value.") Integer bitrate;

  String preset;
}
