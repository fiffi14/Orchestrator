/* Copyright 2026 UnitedCloud. All rights reserved. */
package uc.orchestrator.service;

import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uc.orchestrator.repository.StreamerVodInfoLinkRepository;

@Service
@RequiredArgsConstructor
public class DefaultVodAvailabilityService implements VodAvailabilityService {

  private final StreamerVodInfoLinkRepository repository;

  @Override
  public Set<Long> findStreamerIdsByVodId(Long vodId) {
    return repository.findStreamerIdsByVodId(vodId);
  }
}
