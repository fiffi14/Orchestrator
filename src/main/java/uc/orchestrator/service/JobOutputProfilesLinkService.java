/* Copyright 2026 UnitedCloud. All rights reserved. */
package uc.orchestrator.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uc.orchestrator.entity.JobOutputProfilesLink;
import uc.orchestrator.entity.JobOutputProfilesLinkId;
import uc.orchestrator.repository.JobOutputProfilesLinkRepository;

@Service
@RequiredArgsConstructor
public class JobOutputProfilesLinkService {

  private final JobOutputProfilesLinkRepository repository;

  public JobOutputProfilesLink createLink(Long jobId, Long outputProfileId) {
    JobOutputProfilesLink link = new JobOutputProfilesLink();
    link.setId(new JobOutputProfilesLinkId(jobId, outputProfileId));

    return repository.save(link);
  }

  public List<JobOutputProfilesLink> getLinksByJobId(Long jobId) {
    return repository.findByIdJobId(jobId);
  }

  public List<JobOutputProfilesLink> getLinksByOutputProfileId(Long outputProfileId) {
    return repository.findByIdOutputProfileId(outputProfileId);
  }

  public void deleteLink(Long jobId, Long outputProfileId) {

    repository.deleteById(new JobOutputProfilesLinkId(jobId, outputProfileId));
  }

  public void deleteAllLinksForJob(Long jobId) {
    repository.deleteByIdJobId(jobId);
  }
}
