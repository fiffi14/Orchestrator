/* Copyright 2026 UnitedCloud. All rights reserved. */
package uc.orchestrator.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uc.orchestrator.entity.StreamerVodInfoLink;
import uc.orchestrator.entity.StreamerVodInfoLinkId;
import uc.orchestrator.repository.StreamerVodInfoLinkRepository;

@Service
@RequiredArgsConstructor
public class StreamerVodLinkService {

  private final StreamerVodInfoLinkRepository repository;

  public void createLink(Long streamerId, Long vodId) {

    StreamerVodInfoLink link = new StreamerVodInfoLink();

    link.setId(new StreamerVodInfoLinkId(streamerId, vodId));

    repository.save(link);
  }

  public void deleteLink(Long streamerId, Long vodId) {
    repository.deleteById(new StreamerVodInfoLinkId(streamerId, vodId));
  }
}
