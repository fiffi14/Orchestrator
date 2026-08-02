/* Copyright 2026 UnitedCloud. All rights reserved. */
package uc.orchestrator.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

public class OutputProfileServiceException extends RuntimeException {
  @Getter private final String existingConfigName;

  @Getter private final HttpStatus status;

  public OutputProfileServiceException(HttpStatus status, String config) {
    super("Problem with passed config name: " + config);
    existingConfigName = config;
    this.status = status;
  }
}
