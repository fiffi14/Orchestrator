/* Copyright 2026 UnitedCloud. All rights reserved. */
package uc.orchestrator.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "job_info")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TranscodingJob {

  public enum JobStatus {
    PENDING,
    TRANSCODING,
    PACKAGING,
    FINISHED,
    FAILED
  }

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "job_id")
  private Long jobId;

  @Column(name = "input_vod", nullable = false, length = 255)
  private String inputVod;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 50)
  private JobStatus status;

  @Column(name = "fragment_duration", nullable = false)
  private Float fragmentDuration;
}
