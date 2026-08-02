/* Copyright 2026 UnitedCloud. All rights reserved. */
package uc.orchestrator.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

@Data
@JsonPropertyOrder({"source", "logo"})
public class InputConfig {
  private String source;
  private LogoConfig logo;
}
