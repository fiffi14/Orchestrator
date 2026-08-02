/* Copyright 2026 UnitedCloud. All rights reserved. */
package uc.orchestrator.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class VodValidationException extends RuntimeException {

  private final HttpStatus status;

  public VodValidationException(HttpStatus status, String message) {
    super(message);
    this.status = status;
  }
}
