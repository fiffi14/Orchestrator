/* Copyright 2026 UnitedCloud. All rights reserved. */
package uc.orchestrator.dto;

import java.time.Instant;
import lombok.*;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class StreamerResponseDTO {

  private final Long streamerId;
  private final String streamerName;
  private final Integer port;
  private final String rootVodPath;
  private final Integer capacity;
  private final Instant lastHeartbeatTimestamp;
  private Boolean online;
}
