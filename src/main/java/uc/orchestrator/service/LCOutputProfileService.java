/* Copyright 2026 UnitedCloud. All rights reserved. */
package uc.orchestrator.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import uc.orchestrator.constants.StringLiterals;
import uc.orchestrator.dto.LCOutputProfileCreateDTO;
import uc.orchestrator.dto.LCOutputProfileGetDTO;
import uc.orchestrator.dto.LCOutputProfileUpdateDTO;
import uc.orchestrator.entity.LCOutputProfile;
import uc.orchestrator.exception.OutputProfileServiceException;
import uc.orchestrator.mapper.LCOutputProfileMapper;
import uc.orchestrator.repository.LCOutputProfileRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class LCOutputProfileService {

  private final LCOutputProfileRepository repository;

  private final LCOutputProfileMapper mapper;

  private final ObjectMapper jsonMapper;

  @Transactional
  public LCOutputProfileCreateDTO saveOutputProfileConfig(LCOutputProfileCreateDTO profileDTO) {

    if (repository.existsByConfigName(profileDTO.getConfigName())) {
      throw new OutputProfileServiceException(
          HttpStatus.CONFLICT,
          "The configuration for the output profile already exists when creating new row (POST): "
              + profileDTO.getConfigName());
    }
    try {
      LCOutputProfile profile = mapper.convertToEntity(profileDTO);

      LCOutputProfile saved = repository.save(profile);
      return mapper.convertToCreateDTO(saved);

    } catch (DataIntegrityViolationException e) {
      throw new OutputProfileServiceException(
          HttpStatus.CONFLICT,
          "Message: "
              + e.getMessage()
              + " (Two configurations of the same name requested to be saved at the same time: "
              + profileDTO.getConfigName()
              + ")");
    }
  }

  public List<LCOutputProfileGetDTO> getAllOutputProfileConfigs() {
    return repository.findAll().stream().map(mapper::convertToGetDTO).toList();
  }

  public LCOutputProfileGetDTO getOutputProfileByConfigName(String configName) {

    Optional<LCOutputProfile> profile = repository.findByConfigName(configName);
    if (profile.isEmpty()) {
      log.warn("The targeted configuration doesn't exist (GET): " + configName);
      throw new OutputProfileServiceException(
          HttpStatus.NOT_FOUND, "The targeted configuration doesn't exist (GET): " + configName);
    }
    LCOutputProfile fetched = profile.get();
    return mapper.convertToGetDTO(fetched);
  }

  @Transactional
  public void deleteOutputProfileConfig(String configName) {

    if (!repository.existsByConfigName(configName)) {
      log.warn("The targeted configuration doesn't exist (DELETE): " + configName);
      throw new OutputProfileServiceException(
          HttpStatus.NOT_FOUND, "The targeted configuration doesn't exist (DELETE): " + configName);
    }

    repository.deleteByConfigName(configName);
  }

  @Transactional
  public LCOutputProfileUpdateDTO partialUpdateOutputProfileConfig(
      String configName, LCOutputProfileUpdateDTO patchDTO) {
    LCOutputProfile profile =
        repository
            .findByConfigName(configName)
            .orElseThrow(
                () ->
                    new OutputProfileServiceException(
                        HttpStatus.NOT_FOUND,
                        "The targeted configuration for the update doesn't exist (PATCH): "
                            + configName));

    mapper.updateEntityFromDto(patchDTO, profile);

    try {
      LCOutputProfile saved = repository.saveAndFlush(profile);
      return mapper.convertToUpdateDTO(saved);
    } catch (DataIntegrityViolationException e) {
      throw new OutputProfileServiceException(
          HttpStatus.CONFLICT,
          "The configuration name already exists: " + patchDTO.getConfigName());
    }
  }

  public List<String> getConfigNamesByIds(List<Long> profileIds) {

    if (profileIds == null || profileIds.isEmpty()) {
      throw new OutputProfileServiceException(HttpStatus.BAD_REQUEST, "Profile ID list is empty");
    }

    List<LCOutputProfile> profiles = repository.findAllByIdIn(profileIds);
    List<String> configNames = new ArrayList<>();

    for (LCOutputProfile profile : profiles) {
      configNames.add(profile.getConfigName());
    }

    if (configNames.isEmpty()) {
      throw new OutputProfileServiceException(
          HttpStatus.NOT_FOUND, "No output profiles found for provided IDs");
    }

    return configNames;
  }

  private double getFrameRateForAsset(Path assetPath) {
    List<String> command =
        List.of(
            "ffprobe",
            "-v",
            "error",
            "-select_streams",
            "v:0",
            "-show_entries",
            "stream=r_frame_rate",
            "-of",
            "json",
            assetPath.toAbsolutePath().toString());

    try {
      Process process = new ProcessBuilder(command).start();

      InputStream is = process.getInputStream();
      JsonNode root = jsonMapper.readTree(is);
      JsonNode streams = root.get("streams");

      if (streams != null && streams.isArray() && !streams.isEmpty()) {
        String rawFrameRate = streams.get(0).get("r_frame_rate").asText();
        return parseFrameRate(rawFrameRate);
      }

      int exitCode = process.waitFor();
      if (exitCode != 0) {
        log.error("Exit code when running ffprobe command is not 0, but: " + exitCode);
        throw new OutputProfileServiceException(
            HttpStatus.BAD_REQUEST,
            "Failed to execute ffprobe on asset: "
                + assetPath.toAbsolutePath()
                + " | Because Exit code when running ffprobe command is not 0, but: "
                + exitCode);
      }
    } catch (IOException e) {
      log.error("Failed to execute ffprobe on asset: " + assetPath.toAbsolutePath());
      throw new OutputProfileServiceException(
          HttpStatus.BAD_REQUEST,
          "Failed to execute ffprobe on asset: " + assetPath.toAbsolutePath());

    } catch (InterruptedException e) {
      log.error(
          "Failed to execute ffprobe on asset: " + e.getMessage() + ", cause: " + e.getCause());
      Thread.currentThread()
          .interrupt(); // Restore interrupted state so the execution pool can track thread health
      throw new OutputProfileServiceException(
          HttpStatus.BAD_REQUEST,
          "Failed to execute ffprobe on asset: " + e.getMessage() + ", cause: " + e.getCause());
    }

    return 0.0;
  }

  private double parseFrameRate(String rawFrameRate) {
    if (rawFrameRate == null || rawFrameRate.isEmpty()) {
      return 0.0;
    }

    if (rawFrameRate.contains("/")) {
      String[] fractionParts = rawFrameRate.trim().split("/");
      double numerator = Double.parseDouble(fractionParts[0]);
      double denominator = Double.parseDouble(fractionParts[1]);

      if (denominator == 0) {
        return 0.0;
      }

      return numerator / denominator;
    }

    return Double.parseDouble(rawFrameRate);
  }

  private StringBuilder getHlsHeaderBuilder(Long configId, Path assetPath) {
    StringBuilder header = new StringBuilder();

    Optional<LCOutputProfile> profile = repository.findById(configId);

    if (profile.isEmpty()) {
      throw new OutputProfileServiceException(
          HttpStatus.BAD_REQUEST, "Specified config id is not found - forming HLS header.");
    }

    LCOutputProfile profileEntity = profile.get();

    header
        .append(StringLiterals.EXT_INF_STREAM_TAG)
        .append(":")
        .append(StringLiterals.BANDWIDTH_TAG)
        .append("=")
        .append(profileEntity.getBitrate() * 1000)
        .append(",")
        .append(StringLiterals.FRAMERATE_TAG)
        .append("=")
        .append(getFrameRateForAsset(assetPath))
        .append(",")
        .append(StringLiterals.RESOLUTION_TAG)
        .append("=")
        .append(profileEntity.getHeight())
        .append("x")
        .append(profileEntity.getWidth());

    return header;
  }

  public void createHlsHeaderFile(Long id, Path assetPath, Path filePath) {
    StringBuilder header = getHlsHeaderBuilder(id, assetPath);

    try {
      Files.writeString(filePath, header);
    } catch (IOException e) {
      log.error(
          "Failed to write header into a file. (File path: " + filePath + ", asset: " + assetPath);
      throw new OutputProfileServiceException(
          HttpStatus.BAD_REQUEST,
          "Failed to write header into a file. (File path: " + filePath + ", asset: " + assetPath);
    }
  }
}
