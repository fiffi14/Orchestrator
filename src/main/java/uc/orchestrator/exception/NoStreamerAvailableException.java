/* Copyright 2026 UnitedCloud. All rights reserved. */
package uc.orchestrator.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

public class NoStreamerAvailableException extends RuntimeException {

  @Getter private final HttpStatus status;

  public NoStreamerAvailableException(String asset) {
    super("No streamer available for VOD: " + asset);
    this.status = HttpStatus.NOT_FOUND;
  }
}
