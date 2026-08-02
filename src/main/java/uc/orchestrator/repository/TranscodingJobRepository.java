/* Copyright 2026 UnitedCloud. All rights reserved. */
package uc.orchestrator.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uc.orchestrator.entity.TranscodingJob;

@Repository
public interface TranscodingJobRepository extends JpaRepository<TranscodingJob, Long> {

  Optional<TranscodingJob> findByJobId(Long jobId);
}
