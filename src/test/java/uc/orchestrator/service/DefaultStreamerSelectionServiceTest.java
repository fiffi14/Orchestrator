/* Copyright 2026 UnitedCloud. All rights reserved. */
package uc.orchestrator.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uc.orchestrator.entity.StreamerInfo;
import uc.orchestrator.kafka.StreamerState;
import uc.orchestrator.kafka.StreamerStateService;
import uc.orchestrator.repository.StreamerRepository;

@ExtendWith(MockitoExtension.class)
class DefaultStreamerSelectionServiceTest {

  @Mock private VodAvailabilityService vodAvailabilityService;

  @Mock private StreamerStateService streamerStateService;

  @Mock private StreamerRepository streamerRepository;

  @Mock private StreamerService streamerService;

  @InjectMocks private DefaultStreamerSelectionService service;

  @Test
  @DisplayName("Should select the streamer with the lowest number of active sessions")
  void shouldSelectLeastLoadedStreamer() {
    when(vodAvailabilityService.findStreamerIdsByVodId(1L)).thenReturn(Set.of(1L, 2L));

    when(streamerStateService.get("s1"))
        .thenReturn(new StreamerState("s1", 5, System.currentTimeMillis()));

    when(streamerStateService.get("s2"))
        .thenReturn(new StreamerState("s2", 1, System.currentTimeMillis()));

    when(streamerRepository.findById(1L))
        .thenReturn(Optional.of(new StreamerInfo(1L, "s1", 8081, "/vod", 10, null)));

    when(streamerRepository.findById(2L))
        .thenReturn(Optional.of(new StreamerInfo(2L, "s2", 8082, "/vod", 10, null)));

    when(streamerService.isOnline(any(StreamerInfo.class))).thenReturn(true);

    Optional<StreamerCandidate> result = service.selectBestStreamer(1L);

    assertThat(result).isPresent();
    assertThat(result.get().streamerId()).isEqualTo(2L);
  }

  @Test
  @DisplayName("Should return empty optional when vodId is null")
  void shouldReturnEmptyForNullVodId() {
    assertThat(service.selectBestStreamer(null)).isEmpty();
  }

  @Test
  @DisplayName("Should return empty optional when no streamer contains requested VOD")
  void shouldReturnEmptyWhenNoStreamerContainsVod() {
    when(vodAvailabilityService.findStreamerIdsByVodId(1L)).thenReturn(Set.of());

    Optional<StreamerCandidate> result = service.selectBestStreamer(1L);

    assertThat(result).isEmpty();
  }

  @Test
  @DisplayName("Should return empty optional when all available streamers are at maximum capacity")
  void shouldReturnEmptyWhenAllStreamersAreAtCapacity() {
    when(vodAvailabilityService.findStreamerIdsByVodId(1L)).thenReturn(Set.of(1L));

    when(streamerStateService.get("s1"))
        .thenReturn(new StreamerState("s1", 10, System.currentTimeMillis()));

    when(streamerRepository.findById(1L))
        .thenReturn(Optional.of(new StreamerInfo(1L, "s1", 8081, "/vod", 10, null)));

    when(streamerService.isOnline(any(StreamerInfo.class))).thenReturn(true);

    Optional<StreamerCandidate> result = service.selectBestStreamer(1L);

    assertThat(result).isEmpty();
  }

  @Test
  @DisplayName("Should ignore streamer and return empty optional if its state is missing")
  void shouldIgnoreStreamerWithoutState() {
    when(vodAvailabilityService.findStreamerIdsByVodId(1L)).thenReturn(Set.of(1L));

    when(streamerStateService.get("s1")).thenReturn(null);

    Optional<StreamerCandidate> result = service.selectBestStreamer(1L);

    assertThat(result).isEmpty();
  }
}
