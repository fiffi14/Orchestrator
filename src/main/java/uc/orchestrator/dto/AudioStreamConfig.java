/* Copyright 2026 UnitedCloud. All rights reserved. */
package uc.orchestrator.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

@Data
@JsonPropertyOrder({"pid", "codec"})
public class AudioStreamConfig {
  private int pid;
  private String codec;
}
