/* Copyright 2026 UnitedCloud. All rights reserved. */
package uc.orchestrator.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uc.orchestrator.exception.NoStreamerAvailableException;
import uc.orchestrator.kafka.StreamerStateService;
import uc.orchestrator.repository.VodRepository;

@ExtendWith(MockitoExtension.class)
class VodPlaybackServiceTest {

  @Mock private StreamerSelectionService selectionService;
  @Mock private VodRepository vodRepository;
  @Mock private StreamerStateService streamerStateService;

  private VodPlaybackService service;

  @BeforeEach
  void setUp() {
    service = new VodPlaybackService(selectionService, vodRepository, streamerStateService);
  }

  @Test
  @DisplayName("Should return redirect URI when streamer is available")
  void shouldReturnRedirectUri() {
    StreamerCandidate candidate = new StreamerCandidate(1L, "r1", 5, 100, 8090);

    when(vodRepository.findAssetIdByAssetName("movie_1")).thenReturn(Optional.of(42L));
    when(selectionService.selectBestStreamer(42L)).thenReturn(Optional.of(candidate));

    URI result = service.resolveRedirect("movie_1", "m3u8", "abc123");

    assertThat(result)
        .isNotNull()
        .isEqualTo(
            URI.create(
                "http://localhost:8090/api/v1/vod/stream?asset=movie_1&player=m3u8&session=abc123"));
  }

  @Test
  @DisplayName("Should generate random short session when incoming session is null or empty")
  void shouldGenerateUuidSessionWhenMissing() {

    StreamerCandidate candidate = new StreamerCandidate(1L, "r1", 5, 100, 8090);

    when(vodRepository.findAssetIdByAssetName("movie_1")).thenReturn(Optional.of(42L));
    when(selectionService.selectBestStreamer(42L)).thenReturn(Optional.of(candidate));

    URI result = service.resolveRedirect("movie_1", "m3u8", null);

    assertThat(result).isNotNull();

    String query = result.getQuery();

    assertThat(query).contains("asset=movie_1").contains("player=m3u8").contains("session=");

    String sessionValue =
        java.util.Arrays.stream(query.split("&"))
            .filter(p -> p.startsWith("session="))
            .map(p -> p.substring("session=".length()))
            .findFirst()
            .orElseThrow();

    assertThat(sessionValue).hasSize(8);
  }

  @Test
  @DisplayName("Should throw exception when no streamer available")
  void shouldThrowExceptionWhenNoStreamer() {
    when(vodRepository.findAssetIdByAssetName("movie_1")).thenReturn(Optional.of(42L));
    when(selectionService.selectBestStreamer(42L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.resolveRedirect("movie_1", "m3u8", "abc123"))
        .isInstanceOf(NoStreamerAvailableException.class)
        .hasMessageContaining("movie_1");
  }

  @Test
  @DisplayName("Should throw exception when asset name is not found in database")
  void shouldThrowExceptionWhenAssetMissingInDb() {
    when(vodRepository.findAssetIdByAssetName("unknown_movie")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.resolveRedirect("unknown_movie", "m3u8", "abc123"))
        .isInstanceOf(NoStreamerAvailableException.class)
        .hasMessageContaining("Asset not found in database");
  }

  @Test
  @DisplayName("Should throw exception when VOD ID is null or blank")
  void shouldThrowExceptionWhenVodIdIsNull() {
    assertThatThrownBy(() -> service.resolveRedirect(null, "m3u8", "abc123"))
        .isInstanceOf(NoStreamerAvailableException.class)
        .hasMessageContaining("VOD ID cannot be null or empty");

    assertThatThrownBy(() -> service.resolveRedirect("   ", "m3u8", "abc123"))
        .isInstanceOf(NoStreamerAvailableException.class)
        .hasMessageContaining("VOD ID cannot be null or empty");
  }
}
