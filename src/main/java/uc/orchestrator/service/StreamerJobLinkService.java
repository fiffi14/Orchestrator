/* Copyright 2026 UnitedCloud. All rights reserved. */
package uc.orchestrator.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uc.orchestrator.entity.StreamerJobInfoLink;
import uc.orchestrator.entity.StreamerJobInfoLinkId;
import uc.orchestrator.repository.StreamerJobInfoLinkRepository;

@Service
@RequiredArgsConstructor
public class StreamerJobLinkService {

  private final StreamerJobInfoLinkRepository repository;

  public void createLink(Long streamerId, Long jobId) {

    StreamerJobInfoLink link = new StreamerJobInfoLink();

    link.setId(new StreamerJobInfoLinkId(streamerId, jobId));

    repository.save(link);
  }

  public void deleteLink(Long streamerId, Long jobId) {
    repository.deleteById(new StreamerJobInfoLinkId(streamerId, jobId));
  }
}
