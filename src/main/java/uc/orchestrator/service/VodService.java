/* Copyright 2026 UnitedCloud. All rights reserved. */
package uc.orchestrator.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import uc.orchestrator.entity.Vod;
import uc.orchestrator.repository.VodRepository;

@Service
@RequiredArgsConstructor
public class VodService {

  private final VodRepository vodRepository;
  private final StreamerVodLinkService streamerVodLinkService;
  private final StreamerJobLinkService streamerJobLinkService;

  @Transactional
  public Vod registerVodWithLinks(Long jobId, List<Long> streamerIds, String assetName) {

    if (assetName == null || assetName.isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Asset name is required");
    }

    if (streamerIds == null || streamerIds.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Streamer list is empty");
    }
    Vod savedVod =
        vodRepository
            .findByAssetName(assetName)
            .orElseGet(
                () -> {
                  Vod newVod = Vod.builder().assetName(assetName).build();
                  return vodRepository.save(newVod);
                });

    for (Long streamerId : streamerIds) {
      streamerVodLinkService.createLink(streamerId, savedVod.getId());
    }

    for (Long streamerId : streamerIds) {
      streamerJobLinkService.createLink(streamerId, jobId);
    }

    return savedVod;
  }
}
