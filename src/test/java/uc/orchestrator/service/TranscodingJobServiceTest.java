/* Copyright 2026 UnitedCloud. All rights reserved. */
package uc.orchestrator.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import uc.orchestrator.dto.TranscodingJobRequestDTO;
import uc.orchestrator.entity.TranscodingJob;
import uc.orchestrator.repository.TranscodingJobRepository;

@ExtendWith(MockitoExtension.class)
class TranscodingJobServiceTest {

  @Mock private TranscodingJobRepository repository;

  @InjectMocks private TranscodingJobService service;

  @Nested
  @DisplayName("Tests for createJob")
  class CreateJobTests {

    @Test
    @DisplayName("Should create transcoding job with PENDING status")
    void createJob_Success() {

      TranscodingJob savedJob = new TranscodingJob();

      savedJob.setJobId(1L);
      savedJob.setInputVod("input.ts");
      savedJob.setFragmentDuration(4.8f);
      savedJob.setStatus(TranscodingJob.JobStatus.PENDING);

      when(repository.save(any(TranscodingJob.class))).thenReturn(savedJob);

      TranscodingJobRequestDTO request = new TranscodingJobRequestDTO();
      request.setInput("inputVod.ts");
      request.setStreamers(List.of(new Long[] {10L}));
      request.setOutputs(List.of(new Long[] {1L, 2L}));
      request.setSegmentLength(4.8f);

      TranscodingJob result = service.createJob(request);

      assertThat(result).isNotNull();
      assertThat(result.getJobId()).isEqualTo(1);
      assertThat(result.getStatus()).isEqualTo(TranscodingJob.JobStatus.PENDING);

      verify(repository, times(1)).save(any(TranscodingJob.class));
    }
  }

  @Nested
  @DisplayName("Tests for updateStatus")
  class UpdateStatusTests {

    @Test
    @DisplayName("Should successfully update job status")
    void updateStatus_Success() {

      TranscodingJob job = new TranscodingJob();

      job.setJobId(1L);
      job.setStatus(TranscodingJob.JobStatus.PENDING);

      when(repository.findById(1L)).thenReturn(Optional.of(job));

      when(repository.save(any(TranscodingJob.class))).thenReturn(job);

      TranscodingJob result = service.updateStatus(1L, TranscodingJob.JobStatus.TRANSCODING);

      assertThat(result.getStatus()).isEqualTo(TranscodingJob.JobStatus.TRANSCODING);

      verify(repository, times(1)).save(job);
    }

    @Test
    @DisplayName("Should throw exception when updating non-existing job")
    void updateStatus_ThrowsNotFound() {

      when(repository.findById(1L)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> service.updateStatus(1L, TranscodingJob.JobStatus.TRANSCODING))
          .isInstanceOf(RuntimeException.class)
          .hasMessageContaining("Job not found");

      verify(repository, never()).save(any());
    }
  }

  @Nested
  @DisplayName("Tests for deleteJob")
  class DeleteJobTests {

    @Test
    @DisplayName("Should delete PENDING job")
    void deleteJob_Success() {

      TranscodingJob job = new TranscodingJob();

      job.setJobId(1L);
      job.setStatus(TranscodingJob.JobStatus.PENDING);

      when(repository.findById(1L)).thenReturn(Optional.of(job));

      service.deleteJob(1L);

      verify(repository, times(1)).delete(job);
    }

    @Test
    @DisplayName("Should throw NOT_FOUND when deleting missing job")
    void deleteJob_NotFound() {

      when(repository.findById(1L)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> service.deleteJob(1L))
          .isInstanceOf(ResponseStatusException.class)
          .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
          .isEqualTo(HttpStatus.NOT_FOUND);

      verify(repository, never()).delete(any());
    }

    @Test
    @DisplayName("Should throw BAD_REQUEST when deleting non-PENDING job")
    void deleteJob_InvalidState() {

      TranscodingJob job = new TranscodingJob();

      job.setStatus(TranscodingJob.JobStatus.FINISHED);

      when(repository.findById(1L)).thenReturn(Optional.of(job));

      assertThatThrownBy(() -> service.deleteJob(1L))
          .isInstanceOf(ResponseStatusException.class)
          .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
          .isEqualTo(HttpStatus.BAD_REQUEST);

      verify(repository, never()).delete(any());
    }
  }

  @Nested
  @DisplayName("Tests for getJobById")
  class GetJobByIdTests {

    @Test
    @DisplayName("Should return job when it exists")
    void getJobById_Success() {

      TranscodingJob job = new TranscodingJob();

      job.setJobId(1L);

      when(repository.findById(1L)).thenReturn(Optional.of(job));

      TranscodingJob result = service.getJobById(1L);

      assertThat(result).isNotNull();
      assertThat(result.getJobId()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should throw NOT_FOUND when job doesn't exist")
    void getJobById_NotFound() {

      when(repository.findById(1L)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> service.getJobById(1L))
          .isInstanceOf(ResponseStatusException.class)
          .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
          .isEqualTo(HttpStatus.NOT_FOUND);
    }
  }

  @Nested
  @DisplayName("Tests for getAllJobs")
  class GetAllJobsTests {

    @Test
    @DisplayName("Should return all transcoding jobs")
    void getAllJobs_Success() {

      when(repository.findAll()).thenReturn(List.of(new TranscodingJob(), new TranscodingJob()));

      List<TranscodingJob> result = service.getAllJobs();

      assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("Should return empty list when no jobs exist")
    void getAllJobs_EmptyList() {

      when(repository.findAll()).thenReturn(List.of());

      List<TranscodingJob> result = service.getAllJobs();

      assertThat(result).isEmpty();
    }
  }
}
