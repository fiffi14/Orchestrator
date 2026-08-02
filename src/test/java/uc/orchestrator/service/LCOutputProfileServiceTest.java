/* Copyright 2026 UnitedCloud. All rights reserved. */
package uc.orchestrator.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import uc.orchestrator.dto.LCOutputProfileCreateDTO;
import uc.orchestrator.dto.LCOutputProfileGetDTO;
import uc.orchestrator.dto.LCOutputProfileUpdateDTO;
import uc.orchestrator.entity.LCOutputProfile;
import uc.orchestrator.exception.OutputProfileServiceException;
import uc.orchestrator.mapper.LCOutputProfileMapper;
import uc.orchestrator.mapper.LCOutputProfileMapperImpl;
import uc.orchestrator.repository.LCOutputProfileRepository;

@ExtendWith(MockitoExtension.class)
class LCOutputProfileServiceTest {

  @Mock private LCOutputProfileRepository repository;

  private final LCOutputProfileMapper mapper = new LCOutputProfileMapperImpl();

  @InjectMocks private LCOutputProfileService service;

  @Mock private ObjectMapper jsonMapper;

  private LCOutputProfile existingProfile;
  private LCOutputProfileCreateDTO defaultCreateDto;
  private LCOutputProfileGetDTO defaultGetDto;
  private LCOutputProfileUpdateDTO defaultUpdateDto;

  @BeforeEach
  void setUp() {
    service = new LCOutputProfileService(repository, mapper, jsonMapper);

    existingProfile =
        LCOutputProfile.builder()
            .configName("hd_profile")
            .videoCodec("h264")
            .audioCodec("aac")
            .width(1920)
            .height(1080)
            .preset("fast")
            .bitrate(5000)
            .build();

    defaultCreateDto =
        LCOutputProfileCreateDTO.builder()
            .configName("hd_profile")
            .videoCodec("h264")
            .audioCodec("aac")
            .width(1920)
            .height(1080)
            .bitrate(5000)
            .preset("fast")
            .build();

    defaultGetDto =
        LCOutputProfileGetDTO.builder()
            .configName("hd_profile")
            .videoCodec("h264")
            .audioCodec("aac")
            .width(1920)
            .height(1080)
            .bitrate(5000)
            .preset("fast")
            .build();

    defaultUpdateDto =
        LCOutputProfileUpdateDTO.builder()
            .configName("hd_profile")
            .videoCodec("h264")
            .audioCodec("aac")
            .width(1920)
            .height(1080)
            .bitrate(5000)
            .preset("fast")
            .build();
  }

  @Nested
  @DisplayName("Tests for saveOutputProfileConfig")
  class SaveOutputProfileConfigTests {

    @Test
    @DisplayName("Should successfully save configuration when name is unique")
    void saveConfig_Success() {
      when(repository.existsByConfigName(defaultCreateDto.getConfigName())).thenReturn(false);
      when(repository.save(any(LCOutputProfile.class))).thenReturn(existingProfile);

      LCOutputProfileCreateDTO result = service.saveOutputProfileConfig(defaultCreateDto);

      assertThat(result).isNotNull();
      assertThat(result.getConfigName()).isEqualTo("hd_profile");

      verify(repository, times(1)).save(any(LCOutputProfile.class));
    }

    @Test
    @DisplayName("Should throw CONFLICT exception when configuration name already exists")
    void saveConfig_ThrowsConflictException() {
      when(repository.existsByConfigName(defaultCreateDto.getConfigName())).thenReturn(true);

      assertThatThrownBy(() -> service.saveOutputProfileConfig(defaultCreateDto))
          .isInstanceOf(OutputProfileServiceException.class)
          .hasMessageContaining("already exists")
          .extracting(ex -> ((OutputProfileServiceException) ex).getStatus())
          .isEqualTo(HttpStatus.CONFLICT);

      verify(repository, never()).save(any(LCOutputProfile.class));
    }
  }

  @Nested
  @DisplayName("Tests for getAllOutputProfileConfigs")
  class GetAllOutputProfileConfigsTests {

    @Test
    @DisplayName("Should return a list of configurations when records exist")
    void getAllConfigs_ReturnsList() {
      when(repository.findAll()).thenReturn(List.of(existingProfile));

      List<LCOutputProfileGetDTO> result = service.getAllOutputProfileConfigs();

      assertThat(result).isNotEmpty().contains(defaultGetDto);
    }

    @Test
    @DisplayName("Should return an empty list when no configurations exist")
    void getAllConfigs_ReturnsEmptyList() {
      when(repository.findAll()).thenReturn(Collections.emptyList());

      List<LCOutputProfileGetDTO> result = service.getAllOutputProfileConfigs();

      assertThat(result).isEmpty();
    }
  }

  @Nested
  @DisplayName("Tests for deleteOutputProfileConfig")
  class DeleteOutputProfileConfigTests {

    @Test
    @DisplayName("Should successfully delete configuration when it exists")
    void deleteConfig_Success() {
      String configName = "hd_profile";
      when(repository.existsByConfigName(configName)).thenReturn(true);

      service.deleteOutputProfileConfig(configName);

      verify(repository, times(1)).deleteByConfigName(configName);
    }

    @Test
    @DisplayName("Should throw NOT_FOUND exception when targeted configuration doesn't exist")
    void deleteConfig_ThrowsNotFoundException() {
      String configName = "non_existent";
      when(repository.existsByConfigName(configName)).thenReturn(false);

      assertThatThrownBy(() -> service.deleteOutputProfileConfig(configName))
          .isInstanceOf(OutputProfileServiceException.class)
          .extracting(ex -> ((OutputProfileServiceException) ex).getStatus())
          .isEqualTo(HttpStatus.NOT_FOUND);

      verify(repository, never()).deleteByConfigName(anyString());
    }
  }

  @Nested
  @DisplayName("Testes for partialUpdateOutputProfileConfig")
  class UpdateOutputProfileConfigTests {

    @Test
    @DisplayName("Should throw NOT_FOUND exception when configuration to update does not exist")
    void partialUpdate_TargetNotFound() {
      when(repository.findByConfigName("missing")).thenReturn(Optional.empty());

      assertThatThrownBy(
              () -> service.partialUpdateOutputProfileConfig("missing", defaultUpdateDto))
          .isInstanceOf(OutputProfileServiceException.class)
          .extracting(ex -> ((OutputProfileServiceException) ex).getStatus())
          .isEqualTo(HttpStatus.NOT_FOUND);

      verify(repository, never()).save(any(LCOutputProfile.class));
    }

    private static Stream<Arguments> providePatchSituations() {
      return Stream.of(
          Arguments.of(
              "Situation 1: Completely Empty Payload (No modifications expected)",
              LCOutputProfileUpdateDTO.builder().build(),
              0,
              "h264",
              "aac",
              1920,
              1080,
              5000,
              "fast"),
          Arguments.of(
              "Situation 2: Updating a single text field (videoCodec)",
              LCOutputProfileUpdateDTO.builder().videoCodec("av1").build(),
              1,
              "av1",
              "aac",
              1920,
              1080,
              5000,
              "fast"),
          Arguments.of(
              "Situation 3: Updating a single numeric field (bitrate)",
              LCOutputProfileUpdateDTO.builder().bitrate(2500).build(),
              1,
              "h264",
              "aac",
              1920,
              1080,
              2500,
              "fast"),
          Arguments.of(
              "Situation 4: Modifying multiple mixed fields simultaneously",
              LCOutputProfileUpdateDTO.builder()
                  .videoCodec("hevc")
                  .audioCodec("opus")
                  .width(3840)
                  .height(2160)
                  .build(),
              4,
              "hevc",
              "opus",
              3840,
              2160,
              5000,
              "fast"),
          Arguments.of(
              "Situation 5: Full sweep update of all normal fields",
              LCOutputProfileUpdateDTO.builder()
                  .videoCodec("vp9")
                  .audioCodec("flac")
                  .width(1280)
                  .height(720)
                  .bitrate(1500)
                  .preset("slow")
                  .build(),
              6,
              "vp9",
              "flac",
              1280,
              720,
              1500,
              "slow"),
          Arguments.of(
              "Situation 6: Explicit values provided but they completely match current database state",
              LCOutputProfileUpdateDTO.builder()
                  .configName("hd_profile")
                  .videoCodec("h264")
                  .audioCodec("aac")
                  .width(1920)
                  .height(1080)
                  .bitrate(5000)
                  .preset("fast")
                  .build(),
              0,
              "h264",
              "aac",
              1920,
              1080,
              5000,
              "fast"));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("providePatchSituations")
    @DisplayName("Parameterized Scenarios for Partial/Full Updates")
    void partialUpdate_ParameterizedScenarios(
        String scenarioName,
        LCOutputProfileUpdateDTO testDto,
        Integer id,
        String expectedVideo,
        String expectedAudio,
        Integer expectedWidth,
        Integer expectedHeight,
        Integer expectedBitrate,
        String expectedPreset) {

      existingProfile.setVideoCodec("h264");
      existingProfile.setAudioCodec("aac");
      existingProfile.setWidth(1920);
      existingProfile.setHeight(1080);
      existingProfile.setBitrate(5000);
      existingProfile.setPreset("fast");

      when(repository.findByConfigName("hd_profile")).thenReturn(Optional.of(existingProfile));
      when(repository.saveAndFlush(any(LCOutputProfile.class))).thenReturn(existingProfile);

      LCOutputProfileUpdateDTO result =
          service.partialUpdateOutputProfileConfig("hd_profile", testDto);

      assertThat(result)
          .as("Service returned a null DTO payload due to a mock configuration failure.")
          .isNotNull();

      assertThat(result.getVideoCodec()).isEqualTo(expectedVideo);
      assertThat(result.getAudioCodec()).isEqualTo(expectedAudio);
      assertThat(result.getWidth()).isEqualTo(expectedWidth);
      assertThat(result.getHeight()).isEqualTo(expectedHeight);
      assertThat(result.getBitrate()).isEqualTo(expectedBitrate);
      assertThat(result.getPreset()).isEqualTo(expectedPreset);

      verify(repository, times(1)).saveAndFlush(any(LCOutputProfile.class));
    }

    @Test
    @DisplayName("Should allow renaming configuration when the new name is vacant and unique")
    void partialUpdate_SuccessRenameConfigName() {
      LCOutputProfileUpdateDTO renameDto =
          LCOutputProfileUpdateDTO.builder().configName("4k_profile").build();
      when(repository.findByConfigName("hd_profile")).thenReturn(Optional.of(existingProfile));
      when(repository.saveAndFlush(any(LCOutputProfile.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      LCOutputProfileUpdateDTO result =
          service.partialUpdateOutputProfileConfig("hd_profile", renameDto);

      assertThat(result.getConfigName()).isEqualTo("4k_profile");
      verify(repository, times(1)).saveAndFlush(any(LCOutputProfile.class));
    }

    @Test
    @DisplayName(
        "Should throw exception with CONFLICT status during rename if the target config name belongs to another row")
    void partialUpdate_RenameThrowsConflict() {
      LCOutputProfileUpdateDTO renameDto =
          LCOutputProfileUpdateDTO.builder().configName("existing_taken_name").build();
      when(repository.findByConfigName("hd_profile")).thenReturn(Optional.of(existingProfile));

      when(repository.saveAndFlush(any(LCOutputProfile.class)))
          .thenThrow(new DataIntegrityViolationException("Duplicate key error"));

      assertThatThrownBy(() -> service.partialUpdateOutputProfileConfig("hd_profile", renameDto))
          .isInstanceOf(OutputProfileServiceException.class)
          .extracting(ex -> ((OutputProfileServiceException) ex).getStatus())
          .isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    @DisplayName("Should ignore configName field if the query configName is the same")
    void partialUpdate_SameConfigNameIsIgnored() {
      LCOutputProfileUpdateDTO sameNameDto =
          LCOutputProfileUpdateDTO.builder().configName("hd_profile").videoCodec("h265").build();
      when(repository.findByConfigName("hd_profile")).thenReturn(Optional.of(existingProfile));
      when(repository.saveAndFlush(any(LCOutputProfile.class))).thenReturn(existingProfile);
      LCOutputProfileUpdateDTO result =
          service.partialUpdateOutputProfileConfig("hd_profile", sameNameDto);

      assertThat(result.getConfigName()).isEqualTo("hd_profile");
      assertThat(result.getVideoCodec()).isEqualTo("h265");

      verify(repository, never()).existsByConfigName(anyString());
    }

    @Test
    @DisplayName("Some fields are new, some are null or the same value resulting in no changes")
    void partialUpdate_MixedApplyUpdate() {

      LCOutputProfileUpdateDTO mixedDto =
          LCOutputProfileUpdateDTO.builder().videoCodec("h264").width(3840).build();

      when(repository.findByConfigName("hd_profile")).thenReturn(Optional.of(existingProfile));
      when(repository.saveAndFlush(any(LCOutputProfile.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      LCOutputProfileUpdateDTO result =
          service.partialUpdateOutputProfileConfig("hd_profile", mixedDto);

      assertThat(result.getWidth()).isEqualTo(3840);
      assertThat(result.getVideoCodec()).isEqualTo("h264");
      verify(repository, times(1)).saveAndFlush(any(LCOutputProfile.class));
    }
  }

  @Nested
  @DisplayName("Tests for getOutputProfileByConfigName")
  class GetOutputProfileByConfigNameTests {

    @Test
    @DisplayName("Should return the configuration when it exists in the database")
    void getProfile_Success() {

      String configName = "hd_profile";
      when(repository.findByConfigName(configName)).thenReturn(Optional.of(existingProfile));

      LCOutputProfileGetDTO result = service.getOutputProfileByConfigName(configName);

      assertThat(result).isNotNull().isEqualTo(defaultGetDto);

      assertThat(result.getConfigName()).isEqualTo(configName);
      verify(repository, times(1)).findByConfigName(configName);
    }

    @Test
    @DisplayName("Should throw NOT_FOUND exception when the configuration is missing")
    void getProfile_ThrowsNotFoundException() {
      String configName = "non_existent_profile";
      when(repository.findByConfigName(configName)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> service.getOutputProfileByConfigName(configName))
          .isInstanceOf(OutputProfileServiceException.class)
          .hasMessageContaining("The targeted configuration doesn't exist")
          .extracting(ex -> ((OutputProfileServiceException) ex).getStatus())
          .isEqualTo(HttpStatus.NOT_FOUND);

      verify(repository, times(1)).findByConfigName(configName);
    }
  }

  @Nested
  @DisplayName("Mapper Branch Coverage Strategy")
  class MapperTests {

    @Test
    @DisplayName("Should skip field updates entirely when DTO properties are null")
    void partialUpdate_AllPatchFieldsNull_KeepsOriginalEntityState() {
      LCOutputProfileUpdateDTO completelyNullDto = LCOutputProfileUpdateDTO.builder().build();
      LCOutputProfile freshProfile =
          LCOutputProfile.builder()
              .configName("hd_profile")
              .videoCodec("h264")
              .audioCodec("aac")
              .width(1920)
              .height(1080)
              .bitrate(5000)
              .preset("fast")
              .build();

      when(repository.findByConfigName("hd_profile")).thenReturn(Optional.of(freshProfile));
      when(repository.saveAndFlush(any(LCOutputProfile.class))).thenReturn(freshProfile);

      LCOutputProfileUpdateDTO result =
          service.partialUpdateOutputProfileConfig("hd_profile", completelyNullDto);

      assertThat(result).isNotNull();
      assertThat(result.getVideoCodec()).isEqualTo("h264");
      assertThat(result.getAudioCodec()).isEqualTo("aac");
      assertThat(result.getWidth()).isEqualTo(1920);
      assertThat(result.getHeight()).isEqualTo(1080);
      assertThat(result.getBitrate()).isEqualTo(5000);
      assertThat(result.getPreset()).isEqualTo("fast");
    }

    @Test
    @DisplayName("Should execute all setter branches when every DTO property is populated")
    void partialUpdate_AllPatchFieldsPopulated_ExecutesAllSetterBranches() {
      LCOutputProfileUpdateDTO fullyPopulatedDto =
          LCOutputProfileUpdateDTO.builder()
              .configName("brand_new_name")
              .videoCodec("av1")
              .audioCodec("opus")
              .width(3840)
              .height(2160)
              .bitrate(8000)
              .preset("slow")
              .build();

      LCOutputProfile freshProfile =
          LCOutputProfile.builder()
              .configName("hd_profile")
              .videoCodec("h264")
              .audioCodec("aac")
              .width(1920)
              .height(1080)
              .bitrate(5000)
              .preset("fast")
              .build();

      LCOutputProfile fullyMappedProfile =
          LCOutputProfile.builder()
              .configName("brand_new_name")
              .videoCodec("av1")
              .audioCodec("opus")
              .width(3840)
              .height(2160)
              .bitrate(8000)
              .preset("slow")
              .build();

      when(repository.findByConfigName("hd_profile")).thenReturn(Optional.of(freshProfile));
      when(repository.saveAndFlush(any(LCOutputProfile.class))).thenReturn(fullyMappedProfile);

      LCOutputProfileUpdateDTO result =
          service.partialUpdateOutputProfileConfig("hd_profile", fullyPopulatedDto);

      assertThat(result).isNotNull();
      assertThat(result.getConfigName()).isEqualTo("brand_new_name");
      assertThat(result.getVideoCodec()).isEqualTo("av1");
      assertThat(result.getAudioCodec()).isEqualTo("opus");
      assertThat(result.getWidth()).isEqualTo(3840);
      assertThat(result.getHeight()).isEqualTo(2160);
      assertThat(result.getBitrate()).isEqualTo(8000);
      assertThat(result.getPreset()).isEqualTo("slow");
    }

    @Test
    @DisplayName("Should handle underlying MapStruct framework null safety checks safely")
    void mapperDirect_NullObjectSafetyChecks() {
      LCOutputProfileUpdateDTO nullDTO = null;
      assertThat(mapper.convertToUpdateDTO(null)).isNull();
      assertThat(mapper.convertToEntity(nullDTO)).isNull();

      LCOutputProfile sampleProfile = new LCOutputProfile();
      mapper.updateEntityFromDto(null, sampleProfile);

      assertThat(sampleProfile.getConfigName()).isNull();
    }

    @Test
    @DisplayName(
        "Should trigger DataIntegrityViolationException catch block and throw OutputProfileServiceException")
    void partialUpdate_ThrowsConflict_OnDataIntegrityViolation() {
      LCOutputProfileUpdateDTO patchDto =
          LCOutputProfileUpdateDTO.builder()
              .configName("duplicate_name")
              .videoCodec("h265")
              .build();

      LCOutputProfile profile = LCOutputProfile.builder().configName("hd_profile").build();

      when(repository.findByConfigName("hd_profile")).thenReturn(Optional.of(profile));

      when(repository.saveAndFlush(any(LCOutputProfile.class)))
          .thenThrow(
              new org.springframework.dao.DataIntegrityViolationException(
                  "Duplicate key unique constraint"));

      org.junit.jupiter.api.Assertions.assertThrows(
          uc.orchestrator.exception.OutputProfileServiceException.class,
          () -> service.partialUpdateOutputProfileConfig("hd_profile", patchDto),
          "Expected partialUpdate to throw 409 Conflict exception when database breaks constraint");
    }

    @Test
    @DisplayName(
        "Should cover mixed string and numeric field variants for MapStruct branch splitting")
    void partialUpdate_AlternativeMixedFields_HitsOppositeMapperBranches() {
      LCOutputProfileUpdateDTO mixedDto =
          LCOutputProfileUpdateDTO.builder().audioCodec("mp3").height(720).bitrate(2500).build();

      LCOutputProfile profile =
          LCOutputProfile.builder()
              .configName("hd_profile")
              .videoCodec("h264")
              .audioCodec("aac")
              .width(1920)
              .height(1080)
              .bitrate(5000)
              .preset("fast")
              .build();

      when(repository.findByConfigName("hd_profile")).thenReturn(Optional.of(profile));
      when(repository.saveAndFlush(any(LCOutputProfile.class))).thenReturn(profile);

      LCOutputProfileUpdateDTO result =
          service.partialUpdateOutputProfileConfig("hd_profile", mixedDto);

      assertThat(result).isNotNull();
      assertThat(result.getAudioCodec()).isEqualTo("mp3");
      assertThat(result.getHeight()).isEqualTo(720);
      assertThat(result.getVideoCodec()).isEqualTo("h264"); // remained untouched
    }
  }
}
