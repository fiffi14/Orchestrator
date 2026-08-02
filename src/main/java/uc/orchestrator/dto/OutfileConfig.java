/* Copyright 2026 UnitedCloud. All rights reserved. */
package uc.orchestrator.dto;

// import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class OutfileConfig {
  private String destination;
  private String format;
}
