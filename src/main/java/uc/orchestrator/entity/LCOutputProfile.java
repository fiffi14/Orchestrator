/* Copyright 2026 UnitedCloud. All rights reserved. */
package uc.orchestrator.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "lc_output_profile")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LCOutputProfile {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "config_name", nullable = false, unique = true)
  private String configName;

  @Column(name = "audio_codec", nullable = false)
  private String audioCodec;

  @Column(name = "video_codec", nullable = false)
  private String videoCodec;

  @Column(name = "width", nullable = false)
  private Integer width;

  @Column(name = "height", nullable = false)
  private Integer height;

  @Column(name = "bitrate", nullable = false)
  private Integer bitrate;

  @Column(name = "preset", nullable = false)
  private String preset;
}
