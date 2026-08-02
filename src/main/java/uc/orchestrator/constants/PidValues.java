/* Copyright 2026 UnitedCloud. All rights reserved. */
package uc.orchestrator.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PidValues {
  VIDEO_STREAM(1),
  AUDIO_STREAM(2);

  private final int value;
}
