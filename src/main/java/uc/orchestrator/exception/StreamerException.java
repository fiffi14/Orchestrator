/* Copyright 2026 UnitedCloud. All rights reserved. */
package uc.orchestrator.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

public class StreamerException extends RuntimeException {

  @Getter private final HttpStatus status;

  public StreamerException(HttpStatus status, String message) {
    super(message);
    this.status = status;
  }
}
