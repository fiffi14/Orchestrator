/* Copyright 2026 UnitedCloud. All rights reserved. */
package uc.orchestrator.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import uc.orchestrator.constants.StringLiterals;
import uc.orchestrator.dto.LivecoderExecutionConfig;
import uc.orchestrator.dto.LogoDTO;
import uc.orchestrator.entity.LCOutputProfile;
import uc.orchestrator.exception.MappingConfigurationException;
import uc.orchestrator.repository.LCOutputProfileRepository;

@ExtendWith(MockitoExtension.class)
class LivecoderMappingServiceTest {
  @Mock private LCOutputProfileRepository profileRepository;

  private final ObjectMapper objectMapper = new ObjectMapper();

  private LivecoderMappingService mappingService;

  private LCOutputProfile sampleProfile1;
  private LCOutputProfile sampleProfile2;

  @BeforeEach
  void setUp() {

    mappingService = new LivecoderMappingService(objectMapper, profileRepository);

    sampleProfile1 = new LCOutputProfile();
    sampleProfile1.setId(10L);
    sampleProfile1.setConfigName("hd5000");
    sampleProfile1.setVideoCodec("h264");
    sampleProfile1.setAudioCodec("aac");
    sampleProfile1.setPreset("fast");
    sampleProfile1.setWidth(1920);
    sampleProfile1.setHeight(1080);
    sampleProfile1.setBitrate(5000);

    sampleProfile2 = new LCOutputProfile();
    sampleProfile2.setId(20L);
    sampleProfile2.setConfigName("sd2500");
    sampleProfile2.setVideoCodec("h264");
    sampleProfile2.setAudioCodec("aac");
    sampleProfile2.setPreset("medium");
    sampleProfile2.setWidth(1280);
    sampleProfile2.setHeight(720);
    sampleProfile2.setBitrate(2500);
  }

  @Test
  @DisplayName("Should successfully map database configurations into a valid JSON string")
  void when_validInputsProvided_should_returnValidJsonConfigString() {
    String inputVod = "movie.ts";
    Set<Long> profileIds = Set.of(10L, 20L);
    LogoDTO logo = LogoDTO.builder().filePath("logo.png").x(250).y(250).build();
    when(profileRepository.findAllById(anyCollection()))
        .thenReturn(List.of(sampleProfile1, sampleProfile2));

    String jsonResult =
        mappingService.getExecutionConfig(
            inputVod, StringLiterals.DEFAULT_FORMAT, profileIds, logo);

    assertThat(jsonResult)
        .isNotNull()
        .contains("\"source\":\"movie.ts\"")
        .contains("\"bitrate\":5000")
        .contains("\"bitrate\":2500")
        .contains("\"file_path\":\"logo.png\"")
        .contains("\"x\":250")
        .contains("\"y\":250");
    verify(profileRepository, times(1)).findAllById(anyCollection());
  }

  @Test
  @DisplayName("Should throw BAD_REQUEST when output profiles array is null or empty")
  void when_outputProfilesAreNullOrEmpty_should_throwBadRequest() {
    LogoDTO logo = LogoDTO.builder().filePath("logo.png").x(250).y(250).build();

    assertThatThrownBy(
            () ->
                mappingService.getExecutionConfig(
                    "movie.ts", StringLiterals.DEFAULT_FORMAT, null, logo))
        .isInstanceOf(MappingConfigurationException.class)
        .hasFieldOrPropertyWithValue("status", HttpStatus.BAD_REQUEST)
        .hasMessageContaining("Missing output profiles.");

    assertThatThrownBy(
            () ->
                mappingService.getExecutionConfig(
                    "movie.ts", StringLiterals.DEFAULT_FORMAT, Collections.emptySet(), logo))
        .isInstanceOf(MappingConfigurationException.class)
        .hasFieldOrPropertyWithValue("status", HttpStatus.BAD_REQUEST)
        .hasMessageContaining("Missing output profiles.");
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"   ", "\t", "\n"})
  @DisplayName("Should throw BAD_REQUEST when input VOD is null, empty, or blank")
  void when_inputVodIsBlank_should_throwBadRequest(String invalidInputVod) {
    Set<Long> profileIds = Set.of(10L);
    LogoDTO logo = LogoDTO.builder().filePath("logo.png").x(250).y(250).build();

    assertThatThrownBy(
            () ->
                mappingService.getExecutionConfig(
                    invalidInputVod, StringLiterals.DEFAULT_FORMAT, profileIds, logo))
        .isInstanceOf(MappingConfigurationException.class)
        .hasFieldOrPropertyWithValue("status", HttpStatus.BAD_REQUEST)
        .hasMessageContaining("Missing input VOD for transcoding.");
  }

  @Test
  @DisplayName("Should not pass cleanly when logoDTO is completely null")
  void when_logoDtoIsNull_should_buildConfigWithoutLogo() {

    assertThatThrownBy(
            () ->
                mappingService.getExecutionConfig(
                    "movie.mp4", StringLiterals.DEFAULT_FORMAT, Set.of(10L), null))
        .isInstanceOf(MappingConfigurationException.class)
        .hasFieldOrPropertyWithValue("status", HttpStatus.BAD_REQUEST)
        .hasMessageContaining("Logo has not been passed (null).");

    verifyNoInteractions(profileRepository);
  }

  @Test
  @DisplayName("Should throw exception when logoDTO has an empty file path")
  void when_logoPathIsMissing_should_ignoreLogoSafely() {
    LogoDTO emptyLogoDto = LogoDTO.builder().build();

    assertThatThrownBy(
            () ->
                mappingService.buildExecutionConfig(
                    "movie.mp4", StringLiterals.DEFAULT_FORMAT, Set.of(10L), emptyLogoDto))
        .isInstanceOf(MappingConfigurationException.class)
        .hasFieldOrPropertyWithValue("status", HttpStatus.BAD_REQUEST)
        .hasMessageContaining("Logo path has not been passed.");
  }

  @Test
  @DisplayName(
      "Should fall back to coordinates (50,50) when logo path is present but coordinates are null")
  void when_logoCoordinatesAreNull_should_defaultToZeroZero() {
    when(profileRepository.findAllById(anyCollection())).thenReturn(List.of(sampleProfile1));
    LogoDTO logoWithNoCoordinates =
        LogoDTO.builder().filePath("/opt/logo.png").build(); // x and y are left null

    LivecoderExecutionConfig config =
        mappingService.buildExecutionConfig(
            "movie.mp4", StringLiterals.DEFAULT_FORMAT, Set.of(10L), logoWithNoCoordinates);

    assertThat(config).isNotNull();
    assertThat(config.getInput().getLogo()).isNotNull();
    assertThat(config.getInput().getLogo().getFilePath()).isEqualTo("/opt/logo.png");
    assertThat(config.getInput().getLogo().getX()).isEqualTo(50);
    assertThat(config.getInput().getLogo().getY()).isEqualTo(50);
  }

  @Test
  @DisplayName(
      "Should throw NOT_FOUND with missing IDs when some requested profile IDs do not exist in the database")
  void when_someProfileIdsDoNotExistInDb_should_throwNotFoundWithMissingIds() {
    String inputVod = "movie.ts";
    Set<Long> profileIds = Set.of(10L, 20L);
    LogoDTO logo = LogoDTO.builder().filePath("logo.png").x(250).y(250).build();

    when(profileRepository.findAllById(anyCollection())).thenReturn(List.of(sampleProfile1));

    assertThatThrownBy(
            () ->
                mappingService.getExecutionConfig(
                    inputVod, StringLiterals.DEFAULT_FORMAT, profileIds, logo))
        .isInstanceOf(MappingConfigurationException.class)
        .hasFieldOrPropertyWithValue("status", HttpStatus.NOT_FOUND)
        .hasMessageContaining("The following configurations (IDs) do not exists: [20]");

    verify(profileRepository, times(1)).findAllById(anyCollection());
  }

  @Test
  @DisplayName("Should successfully map database configs into a valid JSON string")
  void when_validInputsProvided_should_returnValidJsonConfigString_isolatedTest() throws Exception {
    LCOutputProfileRepository repo = Mockito.mock(LCOutputProfileRepository.class);
    ObjectMapper mapper = new ObjectMapper();
    LivecoderMappingService service = new LivecoderMappingService(mapper, repo);

    String inputVod = "movie.ts";
    Set<Long> profileIds = Set.of(10L, 20L);
    LogoDTO logo = LogoDTO.builder().filePath("logo.png").x(250).y(250).build();

    LCOutputProfile profile1 =
        LCOutputProfile.builder()
            .id(10L)
            .configName("hd5000")
            .videoCodec("h264")
            .audioCodec("aac")
            .preset("fast")
            .width(1920)
            .height(1080)
            .bitrate(5000)
            .build();

    LCOutputProfile profile2 =
        LCOutputProfile.builder()
            .id(20L)
            .configName("sd2500")
            .videoCodec("h264")
            .audioCodec("aac")
            .preset("medium")
            .width(1280)
            .height(720)
            .bitrate(2500)
            .build();

    Mockito.when(repo.findAllById(profileIds)).thenReturn(List.of(profile1, profile2));

    String jsonResult =
        service.getExecutionConfig(inputVod, StringLiterals.DEFAULT_FORMAT, profileIds, logo);

    JsonNode root = mapper.readTree(jsonResult);
    assertThat(root.path("input").path("source").asText()).isEqualTo(inputVod);
    assertThat(root.path("input").path("logo").path("file_path").asText())
        .isEqualTo(logo.getFilePath());
    assertThat(root.path("input").path("logo").path("x").asInt()).isEqualTo(logo.getX());
    assertThat(root.path("input").path("logo").path("y").asInt()).isEqualTo(logo.getY());

    JsonNode outputs = root.path("output");
    assertThat(outputs.isArray()).isTrue();
    assertThat(outputs).isNotEmpty();

    assertThat(outputs.get(0).path("video_stream").path("bitrate").asInt())
        .isEqualTo(profile1.getBitrate());
    assertThat(outputs.get(0).path("video_stream").path("codec").asText())
        .isEqualTo(profile1.getVideoCodec());

    assertThat(outputs.get(1).path("video_stream").path("bitrate").asInt())
        .isEqualTo(profile2.getBitrate());
    assertThat(outputs.get(1).path("video_stream").path("codec").asText())
        .isEqualTo(profile2.getVideoCodec());

    Mockito.verify(repo, times(1)).findAllById(profileIds);
  }
}
