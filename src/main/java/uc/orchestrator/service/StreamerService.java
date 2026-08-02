/* Copyright 2026 UnitedCloud. All rights reserved. */
package uc.orchestrator.service;

import jakarta.transaction.Transactional;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import uc.orchestrator.config.InstantProvider;
import uc.orchestrator.dto.StreamerRegistrationRequestDTO;
import uc.orchestrator.dto.StreamerResponseDTO;
import uc.orchestrator.dto.StreamerUpdateRequestDTO;
import uc.orchestrator.entity.StreamerInfo;
import uc.orchestrator.exception.StreamerException;
import uc.orchestrator.mapper.StreamerMapper;
import uc.orchestrator.repository.StreamerRepository;

@Service
@RequiredArgsConstructor
public class StreamerService {

  private final StreamerRepository repository;
  private final StreamerMapper mapper;
  private final InstantProvider instantProvider;

  private static final int HEARTBEAT_TIMEOUT_SECONDS = 30;

  @Transactional
  public StreamerResponseDTO register(StreamerRegistrationRequestDTO request) {

    validateUniqueConstraintsForRegister(request.getPort(), request.getStreamerName());

    StreamerInfo entity = mapper.toEntity(request);
    entity.setLastHeartbeatTimestamp(instantProvider.now());

    return toResponse(repository.save(entity));
  }

  @Transactional
  public void heartbeat(String streamerName) {

    int updatedRows = repository.updateHeartbeatByName(streamerName, instantProvider.now());

    if (updatedRows == 0) {
      throw new StreamerException(
          HttpStatus.NOT_FOUND, "Streamer not found (HEARTBEAT): " + streamerName);
    }
  }

  public List<StreamerResponseDTO> getAll() {
    return repository.findAll().stream().map(this::toResponse).toList();
  }

  public StreamerResponseDTO getById(long id) {
    return repository
        .findById(id)
        .map(this::toResponse)
        .orElseThrow(
            () -> new StreamerException(HttpStatus.NOT_FOUND, "Streamer not found (GET): " + id));
  }

  @Transactional
  public StreamerResponseDTO update(long id, StreamerUpdateRequestDTO requestDTO) {

    StreamerInfo entity =
        repository
            .findById(id)
            .orElseThrow(
                () ->
                    new StreamerException(
                        HttpStatus.NOT_FOUND, "Streamer not found (PATCH): " + id));

    validateUniqueConstraintsForUpdate(id, requestDTO.getPort(), requestDTO.getStreamerName());

    mapper.updateEntityFromDto(requestDTO, entity);

    try {
      StreamerInfo saved = repository.saveAndFlush(entity);
      return toResponse(repository.save(saved));
    } catch (DataIntegrityViolationException e) {
      throw new StreamerException(
          HttpStatus.CONFLICT, "Error occurred while updating: " + requestDTO.getStreamerName());
    }
  }

  public boolean isOnline(StreamerInfo s) {

    if (s.getLastHeartbeatTimestamp() == null) {
      return false;
    }

    return s.getLastHeartbeatTimestamp()
        .isAfter(instantProvider.now().minusSeconds(HEARTBEAT_TIMEOUT_SECONDS));
  }

  public StreamerInfo getStreamerById(Long streamerId) {

    return repository
        .findByStreamerId(streamerId)
        .orElseThrow(
            () ->
                new StreamerException(
                    HttpStatus.NOT_FOUND, "Streamer not found (GET): " + streamerId));
  }

  private StreamerResponseDTO toResponse(StreamerInfo s) {
    StreamerResponseDTO dto = mapper.toResponseDTO(s);
    dto.setOnline(isOnline(s));
    return dto;
  }

  private void validateUniqueConstraintsForRegister(int port, String name) {

    if (repository.existsByPort(port)) {
      throw new StreamerException(HttpStatus.CONFLICT, "Port already in use");
    }

    if (repository.existsByStreamerName(name)) {
      throw new StreamerException(HttpStatus.CONFLICT, "Streamer name already exists");
    }
  }

  private void validateUniqueConstraintsForUpdate(long id, int port, String name) {

    if (repository.existsByPortAndStreamerIdNot(port, id)) {
      throw new StreamerException(HttpStatus.CONFLICT, "Port already in use");
    }

    if (repository.existsByStreamerNameAndStreamerIdNot(name, id)) {
      throw new StreamerException(HttpStatus.CONFLICT, "Streamer name already exists");
    }
  }
}
