/* Copyright 2026 UnitedCloud. All rights reserved. */
package uc.orchestrator.dto;

public record StreamerEndpoint(int port) {

  private static final String HOST = "localhost";

  public String toBaseUrl() {
    return "http://" + HOST + ":" + port;
  }
}
