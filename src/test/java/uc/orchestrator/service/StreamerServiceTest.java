/* Copyright 2026 UnitedCloud. All rights reserved. */
package uc.orchestrator.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uc.orchestrator.config.InstantProvider;
import uc.orchestrator.dto.StreamerRegistrationRequestDTO;
import uc.orchestrator.dto.StreamerResponseDTO;
import uc.orchestrator.dto.StreamerUpdateRequestDTO;
import uc.orchestrator.entity.StreamerInfo;
import uc.orchestrator.exception.StreamerException;
import uc.orchestrator.mapper.StreamerMapper;
import uc.orchestrator.repository.StreamerRepository;

@ExtendWith(MockitoExtension.class)
class StreamerServiceTest {

  @Mock private StreamerRepository repository;
  @Mock private StreamerMapper mapper;
  @Mock private InstantProvider instantProvider;

  private StreamerService service;

  private final Instant baseTime = Instant.parse("2026-01-01T10:00:00Z");

  @BeforeEach
  void setUp() {
    service = new StreamerService(repository, mapper, instantProvider);
  }

  // ---------------- TEST DATA ----------------

  private StreamerInfo sampleEntity() {
    return StreamerInfo.builder()
        .streamerId(1L)
        .streamerName("test")
        .port(8080)
        .rootVodPath("/vod")
        .capacity(5)
        .lastHeartbeatTimestamp(baseTime)
        .build();
  }

  private StreamerResponseDTO sampleResponse(StreamerInfo s) {
    return StreamerResponseDTO.builder()
        .streamerId(s.getStreamerId())
        .streamerName(s.getStreamerName())
        .port(s.getPort())
        .rootVodPath(s.getRootVodPath())
        .capacity(s.getCapacity())
        .lastHeartbeatTimestamp(s.getLastHeartbeatTimestamp())
        .online(false)
        .build();
  }

  // ---------------- TESTS ----------------

  @Test
  void shouldRegisterStreamerSuccessfully() {

    when(instantProvider.now()).thenReturn(baseTime);

    StreamerRegistrationRequestDTO request =
        StreamerRegistrationRequestDTO.builder()
            .streamerName("test")
            .port(8080)
            .rootVodPath("/vod")
            .capacity(5)
            .build();

    StreamerInfo entity = sampleEntity();

    when(repository.existsByPort(8080)).thenReturn(false);
    when(repository.existsByStreamerName("test")).thenReturn(false);
    when(mapper.toEntity(request)).thenReturn(entity);
    when(repository.save(any())).thenReturn(entity);

    when(mapper.toResponseDTO(any())).thenAnswer(inv -> sampleResponse(inv.getArgument(0)));

    StreamerResponseDTO response = service.register(request);

    assertThat(response.getLastHeartbeatTimestamp()).isEqualTo(baseTime);
  }

  @Test
  void shouldThrowWhenPortExists() {

    StreamerRegistrationRequestDTO request =
        StreamerRegistrationRequestDTO.builder()
            .streamerName("test")
            .port(8080)
            .rootVodPath("/vod")
            .capacity(5)
            .build();

    when(repository.existsByPort(8080)).thenReturn(true);

    assertThatThrownBy(() -> service.register(request)).isInstanceOf(StreamerException.class);
  }

  @Test
  void shouldThrowWhenNameExists() {

    StreamerRegistrationRequestDTO request =
        StreamerRegistrationRequestDTO.builder()
            .streamerName("test")
            .port(8080)
            .rootVodPath("/vod")
            .capacity(5)
            .build();

    when(repository.existsByPort(8080)).thenReturn(false);
    when(repository.existsByStreamerName("test")).thenReturn(true);

    assertThatThrownBy(() -> service.register(request)).isInstanceOf(StreamerException.class);
  }

  @Test
  void shouldUpdateStreamerSuccessfully() {

    when(instantProvider.now()).thenReturn(baseTime);

    StreamerInfo entity = sampleEntity();

    when(repository.findById(1L)).thenReturn(Optional.of(entity));
    when(repository.existsByPortAndStreamerIdNot(9999, 1L)).thenReturn(false);
    when(repository.existsByStreamerNameAndStreamerIdNot("new", 1L)).thenReturn(false);
    when(repository.saveAndFlush(any())).thenReturn(entity);
    when(repository.save(any())).thenReturn(entity);

    doAnswer(
            invocation -> {
              StreamerUpdateRequestDTO dto = invocation.getArgument(0);
              StreamerInfo target = invocation.getArgument(1);
              target.setStreamerName(dto.getStreamerName());
              target.setPort(dto.getPort());
              return null;
            })
        .when(mapper)
        .updateEntityFromDto(any(), any());

    when(mapper.toResponseDTO(any())).thenAnswer(inv -> sampleResponse(inv.getArgument(0)));

    StreamerUpdateRequestDTO updateRequest =
        StreamerUpdateRequestDTO.builder()
            .streamerName("new")
            .port(9999)
            .rootVodPath("/vod2")
            .capacity(20)
            .build();

    StreamerResponseDTO response = service.update(1L, updateRequest);

    assertThat(response.getStreamerName()).isEqualTo("new");
  }

  @Test
  void shouldHeartbeatUpdateTimestamp() {
    String streamerName = "streamer1";
    Instant rightNow = instantProvider.now();
    when(repository.updateHeartbeatByName(streamerName, rightNow)).thenReturn(1);

    service.heartbeat(streamerName);

    verify(repository, times(1)).updateHeartbeatByName(streamerName, rightNow);
  }

  @Test
  void shouldThrowWhenHeartbeatNotFound() {

    String missingStreamer = "unregistered-streamer";
    when(instantProvider.now()).thenReturn(Instant.now());
    when(repository.updateHeartbeatByName(eq(missingStreamer), any(Instant.class))).thenReturn(0);

    assertThatThrownBy(() -> service.heartbeat(missingStreamer))
        .isInstanceOf(StreamerException.class)
        .hasMessageContaining("Streamer not found (HEARTBEAT)");
  }

  @Test
  void shouldDetectOnline() {

    StreamerInfo entity = sampleEntity();
    entity.setLastHeartbeatTimestamp(baseTime.minusSeconds(10));

    when(instantProvider.now()).thenReturn(baseTime);

    assertTrue(service.isOnline(entity));
  }

  @Test
  void shouldDetectOffline() {

    StreamerInfo entity = sampleEntity();
    entity.setLastHeartbeatTimestamp(baseTime.minusSeconds(45));

    when(instantProvider.now()).thenReturn(baseTime);

    assertFalse(service.isOnline(entity));
  }
}
