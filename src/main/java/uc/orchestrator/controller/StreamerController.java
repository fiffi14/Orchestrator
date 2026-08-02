/* Copyright 2026 UnitedCloud. All rights reserved. */
package uc.orchestrator.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uc.orchestrator.dto.StreamerRegistrationRequestDTO;
import uc.orchestrator.dto.StreamerResponseDTO;
import uc.orchestrator.dto.StreamerUpdateRequestDTO;
import uc.orchestrator.service.StreamerService;

@Slf4j
@RestController
@RequestMapping("/api/v1/streamers")
@RequiredArgsConstructor
public class StreamerController {

  private final StreamerService service;

  @Operation(
      summary = "Register new streamer",
      description = "Registers a new streamer instance and stores its configuration in the system.")
  @ApiResponse(
      responseCode = "200",
      description = "Streamer successfully registered",
      content = @Content(schema = @Schema(implementation = StreamerResponseDTO.class)))
  @PostMapping()
  public ResponseEntity<StreamerResponseDTO> registerStreamer(
      @Valid @RequestBody StreamerRegistrationRequestDTO request) {
    StreamerResponseDTO data = service.register(request);
    log.info("Registered streamer: {}", data.getStreamerName());
    return ResponseEntity.status(HttpStatus.CREATED).body(data);
  }

  @Operation(
      summary = "Send heartbeat",
      description = "Updates last heartbeat timestamp for a streamer to mark it as active.")
  @ApiResponse(responseCode = "200", description = "Heartbeat updated")
  @PostMapping("/heartbeat/{name}")
  public void heartbeat(@PathVariable String name) {
    log.info("Heartbeat by streamer: {}", name);
    service.heartbeat(name);
  }

  @Operation(
      summary = "Get all streamers",
      description = "Returns list of all registered streamers with their current status.")
  @ApiResponse(
      responseCode = "200",
      description = "List of streamers",
      content = @Content(schema = @Schema(implementation = StreamerResponseDTO.class)))
  @GetMapping
  public ResponseEntity<List<StreamerResponseDTO>> getAllStreamers() {
    List<StreamerResponseDTO> streamers = service.getAll();

    log.info("Retrieved {} configurations.", streamers.size());
    return ResponseEntity.ok(streamers);
  }

  @Operation(
      summary = "Get streamer by ID",
      description = "Returns a single streamer by its unique identifier.")
  @ApiResponse(
      responseCode = "200",
      description = "Streamer found",
      content = @Content(schema = @Schema(implementation = StreamerResponseDTO.class)))
  @ApiResponse(responseCode = "404", description = "Streamer not found")
  @GetMapping("/{id}")
  public ResponseEntity<StreamerResponseDTO> getStreamerById(@PathVariable long id) {

    StreamerResponseDTO streamer = service.getById(id);
    log.info("Fetching configuration details for: {}", streamer.getStreamerName());
    return ResponseEntity.ok(streamer);
  }

  @Operation(
      summary = "Update streamer",
      description = "Updates streamer configuration fields like port, capacity, and path.")
  @ApiResponse(
      responseCode = "200",
      description = "Streamer updated successfully",
      content = @Content(schema = @Schema(implementation = StreamerResponseDTO.class)))
  @PatchMapping("/{id}")
  public ResponseEntity<StreamerResponseDTO> updateStreamer(
      @PathVariable long id, @Valid @RequestBody StreamerUpdateRequestDTO request) {

    StreamerResponseDTO updatedStreamer = service.update(id, request);

    log.info("Successfully updated the streamer: {}", updatedStreamer.getStreamerName());
    return ResponseEntity.ok(updatedStreamer);
  }
}
