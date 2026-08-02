/* Copyright 2026 UnitedCloud. All rights reserved. */
package uc.orchestrator.service;

import static uc.orchestrator.constants.StringLiterals.DEFAULT_FORMAT;
import static uc.orchestrator.constants.StringLiterals.TMP_PATH;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import uc.orchestrator.constants.PidValues;
import uc.orchestrator.dto.AudioStreamConfig;
import uc.orchestrator.dto.InputConfig;
import uc.orchestrator.dto.LivecoderExecutionConfig;
import uc.orchestrator.dto.LogoConfig;
import uc.orchestrator.dto.LogoDTO;
import uc.orchestrator.dto.OutfileConfig;
import uc.orchestrator.dto.OutputConfig;
import uc.orchestrator.dto.VideoStreamConfig;
import uc.orchestrator.entity.LCOutputProfile;
import uc.orchestrator.exception.MappingConfigurationException;
import uc.orchestrator.repository.LCOutputProfileRepository;

@Slf4j
@Service
@AllArgsConstructor
public class LivecoderMappingService {

  private final ObjectMapper objectMapper;

  private final LCOutputProfileRepository profileRepository;

  public String getExecutionConfig(
      String inputVod, String format, Set<Long> outputProfiles, LogoDTO logoDTO) {
    if (outputProfiles == null || outputProfiles.isEmpty()) {
      throw new MappingConfigurationException(HttpStatus.BAD_REQUEST, "Missing output profiles.");
    }
    if (inputVod == null || inputVod.isBlank()) {
      throw new MappingConfigurationException(
          HttpStatus.BAD_REQUEST, "Missing input VOD for transcoding.");
    }
    String finalFormat = (format == null || format.isBlank()) ? DEFAULT_FORMAT : format;
    try {
      LivecoderExecutionConfig config =
          buildExecutionConfig(inputVod, finalFormat, outputProfiles, logoDTO);
      return objectMapper.writeValueAsString(config);
    } catch (JsonProcessingException e) {
      log.error("Failed to serialize LiveCoder execution config to JSON.", e);
      throw new MappingConfigurationException(
          HttpStatus.EXPECTATION_FAILED,
          "Failed to serialize configurations into LiveCoder JSON format.");
    }
  }

  public LivecoderExecutionConfig buildExecutionConfig(
      String inputVod, String format, Set<Long> outputProfiles, LogoDTO logoDTO) {
    InputConfig input = createInput(inputVod, logoDTO);

    List<LCOutputProfile> dbProfiles = profileRepository.findAllById(outputProfiles);

    if (dbProfiles.size() != outputProfiles.size()) {
      List<Long> foundIDs = dbProfiles.stream().map(LCOutputProfile::getId).toList();
      List<Long> missingIDs = outputProfiles.stream().filter(id -> !foundIDs.contains(id)).toList();

      throw new MappingConfigurationException(
          HttpStatus.NOT_FOUND, "The following configurations (IDs) do not exists: " + missingIDs);
    }

    List<OutputConfig> outputList =
        dbProfiles.stream().map(profile -> createOutput(inputVod, format, profile)).toList();

    LivecoderExecutionConfig config = new LivecoderExecutionConfig();
    config.setInput(input);
    config.setOutput(outputList);

    return config;
  }

  private InputConfig createInput(String inputVod, LogoDTO logoDTO) {
    InputConfig input = new InputConfig();
    input.setSource(System.getProperty("user.home") + "/Orchestrator/Assets/" + inputVod);

    if (logoDTO == null) {
      throw new MappingConfigurationException(
          HttpStatus.BAD_REQUEST, "Logo has not been passed (null).");
    }
    if (logoDTO.getFilePath() == null || logoDTO.getFilePath().isBlank()) {
      throw new MappingConfigurationException(
          HttpStatus.BAD_REQUEST, "Logo path has not been passed.");
    }

    LogoConfig logo = new LogoConfig();
    logo.setFilePath(logoDTO.getFilePath());
    logo.setX(logoDTO.getX() != null ? logoDTO.getX() : 50);
    logo.setY(logoDTO.getY() != null ? logoDTO.getY() : 50);
    input.setLogo(logo);

    return input;
  }

  private String mangleOutputFilepath(String inputVod, String configName) {
    String cleanName = inputVod;
    int lastIndexOfDot = inputVod.lastIndexOf(".");
    if (lastIndexOfDot != -1) {
      cleanName = inputVod.substring(0, lastIndexOfDot);
    }
    return TMP_PATH + cleanName + "_" + configName + ".ts";
  }

  private OutputConfig createOutput(String inputVod, String format, LCOutputProfile profile) {
    OutputConfig out = new OutputConfig();

    VideoStreamConfig video = new VideoStreamConfig();
    video.setPid(PidValues.VIDEO_STREAM.getValue());
    video.setCodec(profile.getVideoCodec());
    video.setPreset(profile.getPreset());
    video.setWidth(profile.getWidth());
    video.setHeight(profile.getHeight());
    video.setBitrate(profile.getBitrate());

    AudioStreamConfig audio = new AudioStreamConfig();
    audio.setPid(PidValues.AUDIO_STREAM.getValue());
    audio.setCodec(profile.getAudioCodec());

    OutfileConfig file = new OutfileConfig();
    file.setFormat(format);

    file.setDestination(mangleOutputFilepath(inputVod, profile.getConfigName()));
    log.info("Generated absolute destination path: {}", file);

    out.setVideoStream(video);
    out.setAudioStream(audio);
    out.setOutfile(file);

    return out;
  }
}
