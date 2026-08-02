/* Copyright 2026 UnitedCloud. All rights reserved. */
package uc.orchestrator.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ProcessExecutionException extends RuntimeException {

  private final HttpStatus status;

  public ProcessExecutionException(HttpStatus status, String message) {
    super(message);
    this.status = status;
  }
}
