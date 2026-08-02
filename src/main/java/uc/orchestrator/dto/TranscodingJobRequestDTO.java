/* Copyright 2026 UnitedCloud. All rights reserved. */
package uc.orchestrator.dto;

import java.util.List;
import lombok.Data;

@Data
public class TranscodingJobRequestDTO {
  private List<Long> outputs;

  private float segmentLength;

  private List<Long> streamers;

  private String input;

  private String logo;
  private int xLogo;
  private int yLogo;
}
