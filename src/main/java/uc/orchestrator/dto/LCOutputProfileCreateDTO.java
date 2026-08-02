/* Copyright 2026 UnitedCloud. All rights reserved. */
package uc.orchestrator.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
@AllArgsConstructor
public class LCOutputProfileCreateDTO {

  @NotBlank(message = "Configuration name is required when adding a configuration.")
  String configName;

  @NotBlank(message = "Video codec name is required when adding a configuration.")
  String videoCodec;

  @NotBlank(message = "Audio codec name is required when adding a configuration.")
  String audioCodec;

  @Positive(message = "Width needs to be a positive value.") Integer width;

  @Positive(message = "Height needs to be a positive value.") Integer height;

  @Positive(message = "Bitrate needs to be a positive value.") Integer bitrate;

  @NotBlank(message = "Preset name is required when adding a configuration.")
  String preset;
}
