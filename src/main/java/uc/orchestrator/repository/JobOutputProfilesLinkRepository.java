/* Copyright 2026 UnitedCloud. All rights reserved. */
package uc.orchestrator.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uc.orchestrator.entity.JobOutputProfilesLink;
import uc.orchestrator.entity.JobOutputProfilesLinkId;

@Repository
public interface JobOutputProfilesLinkRepository
    extends JpaRepository<JobOutputProfilesLink, JobOutputProfilesLinkId> {

  List<JobOutputProfilesLink> findByIdJobId(Long jobId);

  List<JobOutputProfilesLink> findByIdOutputProfileId(Long outputProfileId);

  void deleteByIdJobId(Long jobId);
}
