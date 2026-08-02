/* Copyright 2026 UnitedCloud. All rights reserved. */
package uc.orchestrator.kafka;

public class StreamerState {

  private String streamerName;
  private int activeSessions;
  private long lastUpdate;

  public StreamerState(String streamerId, int activeSessions, long lastUpdate) {
    this.streamerName = streamerId;
    this.activeSessions = activeSessions;
    this.lastUpdate = lastUpdate;
  }

  public String getStreamerName() {
    return streamerName;
  }

  public int getActiveSessions() {
    return activeSessions;
  }

  public long getLastUpdate() {
    return lastUpdate;
  }

  public void setActiveSessions(int activeSessions) {
    this.activeSessions = activeSessions;
  }

  public void setLastUpdate(long lastUpdate) {
    this.lastUpdate = lastUpdate;
  }
}
