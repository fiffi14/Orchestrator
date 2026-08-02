/* Copyright 2026 UnitedCloud. All rights reserved. */
package uc.orchestrator.repository;

import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uc.orchestrator.entity.StreamerVodInfoLink;
import uc.orchestrator.entity.StreamerVodInfoLinkId;

public interface StreamerVodInfoLinkRepository
    extends JpaRepository<StreamerVodInfoLink, StreamerVodInfoLinkId> {

  @Query(
      """
        SELECT l.id.streamerId
        FROM StreamerVodInfoLink l
        WHERE l.id.vodId = :vodId
    """)
  Set<Long> findStreamerIdsByVodId(@Param("vodId") Long vodId);
}
