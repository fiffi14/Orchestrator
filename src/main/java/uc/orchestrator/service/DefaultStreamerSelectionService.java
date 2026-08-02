/* Copyright 2026 UnitedCloud. All rights reserved. */
package uc.orchestrator.service;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uc.orchestrator.entity.StreamerInfo;
import uc.orchestrator.kafka.StreamerState;
import uc.orchestrator.kafka.StreamerStateService;
import uc.orchestrator.repository.StreamerRepository;

@Slf4j
@Service
public class DefaultStreamerSelectionService implements StreamerSelectionService {

  private final VodAvailabilityService vodAvailabilityService;
  private final StreamerStateService streamerStateService;
  private final StreamerRepository streamerRepository;
  private final StreamerService streamerService;

  public DefaultStreamerSelectionService(
      VodAvailabilityService vodAvailabilityService,
      StreamerStateService streamerStateService,
      StreamerRepository streamerRepository,
      StreamerService streamerService) {
    this.vodAvailabilityService = vodAvailabilityService;
    this.streamerStateService = streamerStateService;
    this.streamerRepository = streamerRepository;
    this.streamerService = streamerService;
  }

  @Override
  public Optional<StreamerCandidate> selectBestStreamer(Long vodId) {
    if (vodId == null) {
      return Optional.empty();
    }

    Set<Long> streamerIds = vodAvailabilityService.findStreamerIdsByVodId(vodId);

    if (streamerIds.isEmpty()) {
      return Optional.empty();
    }

    List<StreamerCandidate> candidates =
        streamerIds.stream()
            .map(
                id -> {
                  StreamerInfo info = streamerRepository.findById(id).orElse(null);

                  if (info == null || !streamerService.isOnline(info)) {
                    return null;
                  }

                  StreamerState state = streamerStateService.get(info.getStreamerName());

                  int currentSessions = (state != null) ? state.getActiveSessions() : 0;

                  return new StreamerCandidate(
                      id, info.getStreamerName(), currentSessions, info.getCapacity(), info.getPort());
                })
            .filter(Objects::nonNull)
            .toList();

    if (candidates.isEmpty()) {
      return Optional.empty();
    }

    log.info("Total candidates found: {}", candidates.size());
    candidates.forEach(c -> log.info("Candidate: {} | Sessions: {}/{}", c.streamerName(), c.activeSessions(), c.capacity()));

    List<StreamerCandidate> available =
        candidates.stream().filter(c -> c.activeSessions() < c.capacity()).toList();

    if (available.isEmpty()) {
      return Optional.empty();
    }

    return available.stream()
        .min(
            Comparator.comparingInt(StreamerCandidate::activeSessions)
                .thenComparingLong(StreamerCandidate::streamerId));
  }
}
