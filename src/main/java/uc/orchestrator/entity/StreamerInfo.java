/* Copyright 2026 UnitedCloud. All rights reserved. */
package uc.orchestrator.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "streamer_info")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StreamerInfo {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "streamer_id")
  private Long streamerId;

  @Column(name = "streamer_name", nullable = false, unique = true, length = 20)
  private String streamerName;

  @Column(nullable = false)
  private Integer port;

  @Column(name = "root_vod_path", nullable = false, length = 255)
  private String rootVodPath;

  @Column(name = "capacity", nullable = false)
  private Integer capacity;

  @Column(name = "last_heartbeat_timestamp")
  private Instant lastHeartbeatTimestamp;
}
