/* Copyright 2026 UnitedCloud. All rights reserved. */
package uc.orchestrator.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uc.orchestrator.entity.StreamerJobInfoLink;
import uc.orchestrator.entity.StreamerJobInfoLinkId;

public interface StreamerJobInfoLinkRepository
    extends JpaRepository<StreamerJobInfoLink, StreamerJobInfoLinkId> {}
