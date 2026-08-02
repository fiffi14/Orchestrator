/* Copyright 2026 UnitedCloud. All rights reserved. */
package uc.orchestrator.entity;

import jakarta.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
public class StreamerJobInfoLinkId implements Serializable {

  private Long streamerId;
  private Long jobId;

  public StreamerJobInfoLinkId() {}

  public StreamerJobInfoLinkId(Long streamerId, Long jobId) {
    this.streamerId = streamerId;
    this.jobId = jobId;
  }
}
