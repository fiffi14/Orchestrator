/* Copyright 2026 UnitedCloud. All rights reserved. */
package uc.orchestrator.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

@Data
@JsonPropertyOrder({"pid", "codec", "preset", "width", "height", "bitrate"})
public class VideoStreamConfig {
  private int pid;
  private String codec;
  private String preset;
  private int width;
  private int height;
  private int bitrate;
}
