/* Copyright 2026 UnitedCloud. All rights reserved. */
package uc.orchestrator.service;

import java.util.Optional;

// Service responsible for selecting the best streamer instance for VOD playback.
public interface StreamerSelectionService {

  Optional<StreamerCandidate> selectBestStreamer(Long vodId);
}
