/* Copyright 2026 UnitedCloud. All rights reserved. */
package uc.orchestrator.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "streamer_job_info_link")
public class StreamerJobInfoLink {

  @EmbeddedId private StreamerJobInfoLinkId id;
}
