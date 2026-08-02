/* Copyright 2026 UnitedCloud. All rights reserved. */
package uc.orchestrator.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

public class MappingConfigurationException extends RuntimeException {
  @Getter private final String existingConfigName;

  @Getter private final HttpStatus status;

  public MappingConfigurationException(HttpStatus status, String config) {
    super("Problem with LiveCoder configuration: " + config);
    existingConfigName = config;
    this.status = status;
  }
}
