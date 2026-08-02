/* Copyright 2026 UnitedCloud. All rights reserved. */
package uc.orchestrator.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class JobOutputProfilesLinkId implements Serializable {

  @Column(name = "job_id")
  private Long jobId;

  @Column(name = "output_profile_id")
  private Long outputProfileId;
}
