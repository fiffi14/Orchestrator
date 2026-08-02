/* Copyright 2026 UnitedCloud. All rights reserved. */
package uc.orchestrator.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uc.orchestrator.service.VodPlaybackService;

// Handles VOD playback requests and redirects client to selected streamer.

@RestController
@RequestMapping("/api/v1/vod")
@Tag(name = "VOD Playback API")
public class VodPlaybackController {

  private final VodPlaybackService playbackService;

  public VodPlaybackController(VodPlaybackService playbackService) {
    this.playbackService = playbackService;
  }

  @Operation(summary = "Play VOD", description = "Redirects user to best available streamer")
  @ApiResponse(responseCode = "302", description = "Redirect to streamer")
  @GetMapping()
  public ResponseEntity<Void> playVod(
      @RequestParam("asset") String asset,
      @RequestParam("player") String player,
      @RequestParam(value = "session", required = false) String sessionID) {

    URI redirectUri = playbackService.resolveRedirect(asset, player, sessionID);

    return ResponseEntity.status(HttpStatus.FOUND).location(redirectUri).build();
  }
}
