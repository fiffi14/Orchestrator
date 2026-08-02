/* Copyright 2026 UnitedCloud. All rights reserved. */
package uc.orchestrator.repository;

import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uc.orchestrator.entity.StreamerInfo;

@Repository
public interface StreamerRepository extends JpaRepository<StreamerInfo, Long> {

  boolean existsByPort(int port);

  boolean existsByStreamerName(String streamerName);

  boolean existsByStreamerNameAndStreamerIdNot(String name, long id);

  boolean existsByPortAndStreamerIdNot(int port, long id);

  @Modifying
  @Query("UPDATE StreamerInfo s SET s.lastHeartbeatTimestamp = :now WHERE s.streamerName = :name")
  int updateHeartbeatByName(@Param("name") String name, @Param("now") Instant now);

  Optional<StreamerInfo> findByStreamerId(Long streamerId);
}
