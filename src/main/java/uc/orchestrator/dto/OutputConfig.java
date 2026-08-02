/* Copyright 2026 UnitedCloud. All rights reserved. */
package uc.orchestrator.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

@Data
@JsonPropertyOrder({"video_stream", "audio_stream", "outfile"})
public class OutputConfig {

  @JsonProperty("video_stream")
  private VideoStreamConfig videoStream;

  @JsonProperty("audio_stream")
  private AudioStreamConfig audioStream;

  private OutfileConfig outfile;
}
