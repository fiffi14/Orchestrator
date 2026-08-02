/* Copyright 2026 UnitedCloud. All rights reserved. */
package uc.orchestrator.entity;

import jakarta.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
public class StreamerVodInfoLinkId implements Serializable {

  private Long streamerId;
  private Long vodId;

  public StreamerVodInfoLinkId() {}

  public StreamerVodInfoLinkId(Long streamerId, Long vodId) {
    this.streamerId = streamerId;
    this.vodId = vodId;
  }

  public Long getStreamerId() {
    return streamerId;
  }

  public Long getVodId() {
    return vodId;
  }
}
