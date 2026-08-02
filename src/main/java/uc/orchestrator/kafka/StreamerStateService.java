/* Copyright 2026 UnitedCloud. All rights reserved. */
package uc.orchestrator.kafka;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class StreamerStateService {

  private final Map<String, StreamerState> stateMap = new ConcurrentHashMap<>();

  /** Updates in-memory state based on Kafka event. If streamer does not exist, it is created. */
  public void update(String streamerName, int activeSessions, long timestamp) {

    stateMap.compute(
        streamerName,
        (id, existing) -> {
          if (existing == null) {
            return new StreamerState(streamerName, activeSessions, timestamp);
          }

          // if upcoming event is strictly newer
          if (timestamp > existing.getLastUpdate()) {
            existing.setActiveSessions(activeSessions);
            existing.setLastUpdate(timestamp);
          }
          return existing;
        });
  }

  /** Returns current state for a single streamer. */
  public StreamerState get(String streamerId) {
    return stateMap.get(streamerId);
  }

  /**
   * Returns immutable snapshot of all streamers state. IMPORTANT: prevents external modification.
   */
  public Map<String, StreamerState> getAll() {
    return Collections.unmodifiableMap(stateMap);
  }

  /**
   * Optimistically increments the session count.
   * This bridges the gap until the real Kafka heartbeat arrives.
   */
  public void addPendingSession(String streamerName) {
    stateMap.compute(
        streamerName,
        (name, existingState) -> {
          // If Kafka hasn't populated this yet, create a temporary placeholder with 1 session
          if (existingState == null) {
            return new StreamerState(streamerName, 1, System.currentTimeMillis());
          }

          // Otherwise, just bump the existing count
          existingState.setActiveSessions(existingState.getActiveSessions() + 1);
          return existingState;
        });
  }
}
