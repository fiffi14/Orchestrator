/* Copyright 2026 UnitedCloud. All rights reserved. */
package uc.orchestrator.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uc.orchestrator.entity.Vod;

public interface VodRepository extends JpaRepository<Vod, Long> {
  @Query("SELECT v.id FROM Vod v WHERE v.assetName = :assetName")
  Optional<Long> findAssetIdByAssetName(@Param("assetName") String assetName);

  Optional<Vod> findByAssetName(String assetName);
}
