/* Copyright 2026 UnitedCloud. All rights reserved. */
package uc.orchestrator.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "job_output_profiles_link")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobOutputProfilesLink {

  @EmbeddedId private JobOutputProfilesLinkId id;
}
