/* Copyright 2026 UnitedCloud. All rights reserved. */
package uc.orchestrator.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
@AllArgsConstructor
public class LogoDTO {
  String filePath;
  Integer x;
  Integer y;
}
