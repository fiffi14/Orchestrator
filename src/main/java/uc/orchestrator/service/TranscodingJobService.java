/* Copyright 2026 UnitedCloud. All rights reserved. */
package uc.orchestrator.service;

import static uc.orchestrator.constants.StringLiterals.DEFAULT_FORMAT;
import static uc.orchestrator.constants.StringLiterals.JOB_NOT_FOUND;
import static uc.orchestrator.constants.StringLiterals.TMP_PATH;
import static uc.orchestrator.constants.StringLiterals.USER_HOME;

import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import uc.orchestrator.dto.LogoDTO;
import uc.orchestrator.dto.TranscodingJobRequestDTO;
import uc.orchestrator.entity.TranscodingJob;
import uc.orchestrator.exception.ProcessExecutionException;
import uc.orchestrator.exception.VodValidationException;
import uc.orchestrator.repository.TranscodingJobRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class TranscodingJobService {

  @Value("${transcoding.encoder.path}")
  private String encoderPath;

  private final TranscodingJobRepository jobRepository;
  private final JobOutputProfilesLinkService jobOutputProfilesLinkService;
  private final LivecoderMappingService livecoderMappingService;
  private final LCOutputProfileService lcOutputProfileService;
  private final VodService vodService;
  private final StreamerService streamerService;
  private static final String BASE_DIR = System.getProperty(USER_HOME) + "/Orchestrator";
  private static final Path LOG_DIR = Paths.get(BASE_DIR, "logs");

  @PostConstruct
  public void init() throws IOException {
    Files.createDirectories(LOG_DIR);
  }

  public TranscodingJob createJob(TranscodingJobRequestDTO request) {

    TranscodingJob job = new TranscodingJob();

    job.setInputVod(request.getInput());
    job.setFragmentDuration(request.getSegmentLength());
    job.setStatus(TranscodingJob.JobStatus.PENDING);

    TranscodingJob savedJob = jobRepository.save(job);

    for (Long outputProfileId : request.getOutputs()) {
      jobOutputProfilesLinkService.createLink(savedJob.getJobId(), outputProfileId);
    }

    return savedJob;
  }

  public TranscodingJob updateStatus(Long jobId, TranscodingJob.JobStatus status) {

    TranscodingJob job =
        jobRepository.findById(jobId).orElseThrow(() -> new RuntimeException("JOB_NOT_FOUND"));

    job.setStatus(status);

    return jobRepository.save(job);
  }

  public TranscodingJob.JobStatus getStatus(Long jobId) {
    TranscodingJob job =
        jobRepository
            .findById(jobId)
            .orElseThrow(() -> new RuntimeException(JOB_NOT_FOUND + jobId));

    return job.getStatus();
  }

  public List<TranscodingJob> getAllJobs() {
    return jobRepository.findAll();
  }

  @Transactional
  public void deleteJob(Long jobId) {
    TranscodingJob job =
        jobRepository
            .findById(jobId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, JOB_NOT_FOUND + jobId));

    if (job.getStatus() != TranscodingJob.JobStatus.PENDING) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Cannot delete job thats RUNNING" + jobId);
    }

    jobRepository.delete(job);
  }

  public TranscodingJob getJobById(Long jobId) {
    return jobRepository
        .findById(jobId)
        .orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, JOB_NOT_FOUND + jobId));
  }

  public TranscodingJob.JobStatus getStatusById(Long jobId) {
    TranscodingJob job =
        jobRepository
            .findById(jobId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, JOB_NOT_FOUND + jobId));

    return job.getStatus();
  }

  public void validateInputPath(String inputPath) {

    File file = new File(inputPath);

    if (!file.exists()) {
      throw new VodValidationException(
          HttpStatus.BAD_REQUEST, "Input VOD does not exist: " + inputPath);
    }

    if (!file.isFile()) {
      throw new VodValidationException(
          HttpStatus.BAD_REQUEST, "Input path is not a file: " + inputPath);
    }

    if (!file.canRead()) {
      throw new VodValidationException(
          HttpStatus.FORBIDDEN, "Input VOD is not readable: " + inputPath);
    }
  }

  @Async
  public void transcodeAndPackage(Long jobId, TranscodingJobRequestDTO request) {
    try {
      // Preparing json mapper data

      LogoDTO logo =
          new LogoDTO(
              System.getProperty(USER_HOME) + "/Orchestrator/Assets/" + request.getLogo(),
              request.getXLogo(),
              request.getYLogo());
      Set<Long> outputsSet = new HashSet<>(request.getOutputs());
      String jsonConfig =
          livecoderMappingService.getExecutionConfig(
              request.getInput(), DEFAULT_FORMAT, outputsSet, logo);
      Path configPath = Paths.get("transcodingJobAssets/inputConfig.json");
      // Storing JSON config
      Files.createDirectories(configPath.getParent());
      log.info(jsonConfig);
      Files.writeString(
          configPath, jsonConfig, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

      Process transcoding = startTranscodingProcess(jobId);

      checkExitCode(transcoding, "TRANSCODING", jobId);

      startPackagingProcess(jobId, request);
      log.info("Packaging " + request.getInput() + " DONE");

      String assetName = request.getInput().replace(".ts", "");

      // generate HLS variant headers
      for (Long outputProfileId : request.getOutputs()) {
        lcOutputProfileService.createHlsHeaderFile(
            outputProfileId,
            Paths.get(
                System.getProperty("user.home") + "/Orchestrator/Assets/" + request.getInput()),
            Paths.get(
                System.getProperty(USER_HOME)
                    + "/Orchestrator/Downloads/"
                    + request.getInput().replace(".ts", "/")
                    + lcOutputProfileService
                        .getConfigNamesByIds(Collections.singletonList(outputProfileId))
                        .get(0)
                    + "_descriptor.txt"));
      }

      // Copy segments into Streamer/streamerNAme/assetName
      distributeSegmentsToStreamers(assetName, request.getStreamers());

      vodService.registerVodWithLinks(jobId, request.getStreamers(), assetName);

      log.info("PROCESS DONE!");
    } catch (IOException e) {

      updateStatus(jobId, TranscodingJob.JobStatus.FAILED);

      throw new ProcessExecutionException(
          HttpStatus.INTERNAL_SERVER_ERROR, "I/O error during transcoding: " + e.getMessage());

    } catch (InterruptedException e) {

      Thread.currentThread().interrupt();

      updateStatus(jobId, TranscodingJob.JobStatus.FAILED);

      throw new ProcessExecutionException(
          HttpStatus.INTERNAL_SERVER_ERROR, "Transcoding interrupted");
    }
  }

  public Process startTranscodingProcess(Long jobId) throws IOException {
    String configPath = "transcodingJobAssets/inputConfig.json";

    ProcessBuilder pb =
        new ProcessBuilder(
            encoderPath,
            "-json",
            configPath);

    pb.redirectErrorStream(true);
    pb.redirectOutput(getLogFile(jobId, "transcoding.log"));
    updateStatus(jobId, TranscodingJob.JobStatus.TRANSCODING);

    return pb.start();
  }

  private void startPackagingProcess(Long jobId, TranscodingJobRequestDTO request)
      throws IOException, InterruptedException {

    List<String> outputConfigNames =
        lcOutputProfileService.getConfigNamesByIds(request.getOutputs());
    List<Process> packagingProcesses = new ArrayList<>();
    List<String> outputProfileDirectorys = new ArrayList<>();

    String baseTranscodedVODpath = TMP_PATH + request.getInput().replace(".ts", "_");

    updateStatus(jobId, TranscodingJob.JobStatus.PACKAGING);

    for (String configName : outputConfigNames) { //
      String videoCodec = "libx264";
      String audioCodec = "aac";

      String outputSegmentsDirectory =
          System.getProperty(USER_HOME)
              + "/Orchestrator/Downloads/"
              + request.getInput().replace(".ts", "")
              + "/"
              + configName;
      outputProfileDirectorys.add(outputSegmentsDirectory);
      Files.createDirectories(Paths.get(outputSegmentsDirectory));

      ProcessBuilder packagingProcess =
          new ProcessBuilder(
              "ffmpeg",
              "-i",
              baseTranscodedVODpath + configName + ".ts",
              "-c:v",
              videoCodec,
              "-g",
              "60",
              "-keyint_min",
              "60",
              "-c:a",
              audioCodec,
              "-f",
              "segment",
              "-segment_time",
              Float.toString(request.getSegmentLength()),
              outputSegmentsDirectory
                  + "/seg%04d_"
                  + (int) (request.getSegmentLength() * 1000)
                  + ".ts");

      packagingProcess.redirectErrorStream(true);
      packagingProcess.redirectOutput(getLogFile(jobId, "packaging_" + configName + ".log"));

      Process process = packagingProcess.start();
      packagingProcesses.add(process);
    }

    for (Process process : packagingProcesses) {
      checkExitCode(process, "PACKAGING", jobId);
    }
    // rename last segment to fix segment duration
    for (String directory : outputProfileDirectorys) {
      renameLastSegmentWithRealDuration(
          Paths.get(directory), (int) (request.getSegmentLength() * 1000));
    }

    updateStatus(jobId, TranscodingJob.JobStatus.FINISHED);
  }

  private void checkExitCode(Process process, String stage, Long jobId)
      throws InterruptedException {
    int exitCode = process.waitFor();

    if (exitCode != 0) {
      updateStatus(jobId, TranscodingJob.JobStatus.FAILED);
      throw new ProcessExecutionException(
          HttpStatus.INTERNAL_SERVER_ERROR, stage + " failed with exit code " + exitCode);
    }
  }

  private File getLogFile(Long jobId, String fileName) throws IOException {

    return getJobLogDirectory(jobId).resolve(fileName).toFile();
  }

  private Path getJobLogDirectory(Long jobId) throws IOException {

    Path jobLogDir = LOG_DIR.resolve("job_" + jobId);

    Files.createDirectories(jobLogDir);

    return jobLogDir;
  }

  private void copyDirectory(Path source, Path target) throws IOException {

    try (Stream<Path> paths = Files.walk(source)) {

      for (Path path : paths.toList()) {

        Path relative = source.relativize(path);
        Path destination = target.resolve(relative);

        if (Files.isDirectory(path)) {

          Files.createDirectories(destination);

        } else {

          Files.copy(path, destination, StandardCopyOption.REPLACE_EXISTING);
        }
      }
    }
  }

  private void distributeSegmentsToStreamers(String assetName, List<Long> streamerIds)
      throws IOException {

    Path segmentsSource =
        Paths.get(System.getProperty(USER_HOME), "Orchestrator", "Downloads", assetName);

    for (Long streamerId : streamerIds) {

      Path streamerTarget =
          Paths.get(
              System.getProperty(USER_HOME),
              "streamer_" + streamerService.getStreamerById(streamerId).getStreamerName(),
              assetName);

      Files.createDirectories(streamerTarget);

      copyDirectory(segmentsSource, streamerTarget);
    }
  }

  public void renameLastSegmentWithRealDuration(Path segmentsDir, int segmentLength)
      throws IOException, InterruptedException {

    if (!Files.exists(segmentsDir) || !Files.isDirectory(segmentsDir)) {
      throw new IllegalArgumentException("Invalid segments directory: " + segmentsDir);
    }

    // Finding last segment
    Path lastSegment;
    try (Stream<Path> paths = Files.list(segmentsDir)) {
      lastSegment =
          paths
              .filter(p -> p.toString().endsWith(".ts"))
              .max(Comparator.naturalOrder())
              .orElseThrow(
                  () -> new IllegalStateException("No segment files found in: " + segmentsDir));
    }

    // ffprobe to get real duration
    ProcessBuilder pb =
        new ProcessBuilder(
            "ffprobe",
            "-v",
            "error",
            "-show_entries",
            "format=duration",
            "-of",
            "default=noprint_wrappers=1:nokey=1",
            lastSegment.toString());

    Process process = pb.start();

    String output;
    try (BufferedReader reader =
        new BufferedReader(new InputStreamReader(process.getInputStream()))) {
      output = reader.readLine();
    }

    int exitCode = process.waitFor();
    if (exitCode != 0 || output == null) {
      throw new ProcessExecutionException(
          HttpStatus.INTERNAL_SERVER_ERROR, "ffprobe failed for file: " + lastSegment);
    }

    // Duration in ms
    double durationSec = Double.parseDouble(output.trim());
    long durationMs = Math.round(durationSec * 1000);

    // Rename file
    String newName =
        lastSegment.getFileName().toString().replace(segmentLength + ".ts", durationMs + ".ts");

    Path target = lastSegment.resolveSibling(newName);

    Files.move(lastSegment, target, StandardCopyOption.REPLACE_EXISTING);
  }
}
