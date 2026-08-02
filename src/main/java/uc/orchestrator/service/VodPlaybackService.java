/* Copyright 2026 UnitedCloud. All rights reserved. */
package uc.orchestrator.service;

import java.net.URI;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import uc.orchestrator.exception.NoStreamerAvailableException;
import uc.orchestrator.kafka.StreamerStateService;
import uc.orchestrator.repository.VodRepository;

@Service
public class VodPlaybackService {

  private final StreamerSelectionService selectionService;
  private final VodRepository vodRepository;
  private final StreamerStateService  streamerStateService;

  public VodPlaybackService(
      StreamerSelectionService selectionService, VodRepository vodRepository,
      StreamerStateService streamerStateService) {
    this.selectionService = selectionService;
    this.vodRepository = vodRepository;
    this.streamerStateService = streamerStateService;
  }

  public URI resolveRedirect(String vodId, String player, String sessionID) {

    if (vodId == null || vodId.isBlank()) {
      throw new NoStreamerAvailableException("VOD ID cannot be null or empty");
    }
    String trimmedAsset = vodId.trim();

    String finalSession =
        (sessionID == null || sessionID.isBlank())
            ? UUID.randomUUID().toString().substring(0, 8)
            : sessionID.trim();

    Long numericVodId =
        vodRepository
            .findAssetIdByAssetName(trimmedAsset)
            .orElseThrow(
                () ->
                    new NoStreamerAvailableException(
                        "Asset not found in database: " + trimmedAsset));

    StreamerCandidate streamer =
        selectionService
            .selectBestStreamer(numericVodId)
            .orElseThrow(() -> new NoStreamerAvailableException(vodId));

    streamerStateService.addPendingSession(streamer.streamerName());

    String streamerBaseUrl = "http://localhost:" + streamer.port();

    return UriComponentsBuilder.fromUriString(streamerBaseUrl)
        .path("/api/v1/vod/stream")
        .queryParam("asset", trimmedAsset)
        .queryParam("player", player)
        .queryParam("session", finalSession)
        .build()
        .toUri();
  }
}
