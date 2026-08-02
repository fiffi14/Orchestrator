/* Copyright 2026 UnitedCloud. All rights reserved. */
package uc.orchestrator.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uc.orchestrator.dto.TranscodingJobRequestDTO;
import uc.orchestrator.entity.TranscodingJob;
import uc.orchestrator.service.TranscodingJobService;

@RestController
@RequestMapping("api/v1/job")
@RequiredArgsConstructor
public class TranscodingJobController {

  private final TranscodingJobService jobService;

  @Operation(
      summary = "Create a new transcoding job and start processing pipeline.",
      description =
          "This endpoint creates a new transcoding job, validates the input VOD file, "
              + "persists the job with PENDING status, and asynchronously starts the transcoding "
              + "and packaging pipeline. The processing continues in the background and the "
              + "endpoint immediately returns HTTP 202 Accepted.",
      requestBody =
          @io.swagger.v3.oas.annotations.parameters.RequestBody(
              description =
                  "Payload containing input VOD, output profiles, streamer list and segment length.",
              required = true,
              content =
                  @Content(
                      mediaType = "application/json",
                      schema = @Schema(implementation = TranscodingJobRequestDTO.class))),
      responses = {
        @ApiResponse(
            responseCode = "202",
            description = "Job successfully created and processing started asynchronously."),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request - input VOD validation failed or payload is incorrect.",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = String.class)))
      })
  @PostMapping("/create")
  public ResponseEntity<String> createJob(@RequestBody TranscodingJobRequestDTO request) {
    jobService.validateInputPath(
        System.getProperty("user.home") + "/Orchestrator/Assets/" + request.getInput());
    TranscodingJob job = jobService.createJob(request);
    jobService.transcodeAndPackage(job.getJobId(), request);

    return ResponseEntity.accepted().body("Transcoding started");
  }

  @Operation(
      summary = "Retrieve all transcoding jobs.",
      description = "Returns a list of all transcoding jobs stored in the system.")
  @GetMapping("/getAllJobs")
  public ResponseEntity<List<TranscodingJob>> getAllJobs() {
    return ResponseEntity.ok(jobService.getAllJobs());
  }

  @Operation(
      summary = "Delete a transcoding job by ID.",
      description = "Deletes a job only if it is in PENDING state. ",
      responses = {
        @ApiResponse(responseCode = "200", description = "Job successfully deleted."),
        @ApiResponse(
            responseCode = "400",
            description = "Job cannot be deleted because it is not in PENDING state."),
        @ApiResponse(responseCode = "404", description = "Job not found.")
      })
  @DeleteMapping("/delete/{id}")
  public ResponseEntity<String> deleteJob(@PathVariable Long id) {

    jobService.deleteJob(id);

    return ResponseEntity.ok("Job deleted");
  }

  @Operation(
      summary = "Get transcoding job by ID.",
      description = "Returns full details of a transcoding job",
      responses = {
        @ApiResponse(responseCode = "200", description = "Job found and returned successfully."),
        @ApiResponse(responseCode = "404", description = "Job not found.")
      })
  @GetMapping("/getById/{id}")
  public ResponseEntity<TranscodingJob> getById(@PathVariable Long id) {
    return ResponseEntity.ok(jobService.getJobById(id));
  }

  @Operation(
      summary = "Get status of a transcoding job.",
      description = "Returns only the current execution status of the job. ",
      responses = {
        @ApiResponse(responseCode = "200", description = "Status retrieved successfully."),
        @ApiResponse(responseCode = "404", description = "Job not found.")
      })
  @GetMapping("/status/{id}")
  public ResponseEntity<TranscodingJob.JobStatus> getStatusById(@PathVariable Long id) {

    return ResponseEntity.ok(jobService.getStatusById(id));
  }
}
