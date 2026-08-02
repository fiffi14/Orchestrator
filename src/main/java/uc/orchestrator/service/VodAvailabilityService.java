/* Copyright 2026 UnitedCloud. All rights reserved. */
package uc.orchestrator.service;

import java.util.Set;

public interface VodAvailabilityService {

  Set<Long> findStreamerIdsByVodId(Long vodId);

}
