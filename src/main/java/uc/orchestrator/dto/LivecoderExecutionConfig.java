/* Copyright 2026 UnitedCloud. All rights reserved. */
package uc.orchestrator.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.List;
import lombok.Data;

@JsonPropertyOrder({"input", "output"})
@Data
public class LivecoderExecutionConfig {
  private InputConfig input;
  private List<OutputConfig> output;
}
