/* Copyright 2026 UnitedCloud. All rights reserved. */
package uc.orchestrator.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class LogoConfig {
  @JsonProperty("file_path")
  private String filePath;

  private int x;
  private int y;
}
