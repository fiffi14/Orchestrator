/* Copyright 2026 UnitedCloud. All rights reserved. */
package uc.orchestrator.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;
import uc.orchestrator.dto.LCOutputProfileCreateDTO;
import uc.orchestrator.dto.LCOutputProfileUpdateDTO;
import uc.orchestrator.entity.LCOutputProfile;
import uc.orchestrator.exception.OutputProfileServiceException;
import uc.orchestrator.mapper.LCOutputProfileMapper;
import uc.orchestrator.mapper.LCOutputProfileMapperImpl;
import uc.orchestrator.service.LCOutputProfileService;

@WebMvcTest(LCOutputProfileController.class)
class LCOutputProfileControllerTest {

  @Autowired private MockMvc mockMvc;
  private final LCOutputProfileMapper mapper = Mappers.getMapper(LCOutputProfileMapper.class);
  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private LCOutputProfileService service;

  private LCOutputProfile sampleProfile;
  private LCOutputProfileCreateDTO defaultCreateDto;

  @TestConfiguration
  static class MapStructTestConfig {
    @Bean
    public LCOutputProfileMapper lcOutputProfileMapper() {
      return new LCOutputProfileMapperImpl();
    }
  }

  @BeforeEach
  void setUp() {
    sampleProfile =
        LCOutputProfile.builder()
            .configName("hd_profile")
            .videoCodec("h264")
            .audioCodec("aac")
            .width(1920)
            .height(1080)
            .bitrate(5000)
            .preset("fast")
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
  }

  @Nested
  @DisplayName("GET /outputProfile/config")
  class GetAllConfigsTests {

    @Test
    @DisplayName("Should return 200 OK with list of configs when data exists")
    void getAllConfigs_ReturnsList() throws Exception {
      when(service.getAllOutputProfileConfigs())
          .thenReturn(List.of(mapper.convertToGetDTO(sampleProfile)));

      mockMvc
          .perform(get("/outputProfile/config"))
          .andExpect(status().isOk())
          .andExpect(content().contentType(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$[0].configName").value("hd_profile"))
          .andExpect(jsonPath("$[0].videoCodec").value("h264"));
    }

    @Test
    @DisplayName("Should return 204 NO_CONTENT when database table is empty")
    void getAllConfigs_ReturnsNoContent() throws Exception {
      when(service.getAllOutputProfileConfigs()).thenReturn(Collections.emptyList());

      mockMvc.perform(get("/outputProfile/config")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should return 400 Bad Request when DTO validation fails via @Valid")
    void createConfig_ValidationFails() throws Exception {
      LCOutputProfileCreateDTO invalidDto =
          LCOutputProfileCreateDTO.builder()
              .configName("")
              .videoCodec("")
              .audioCodec("aac")
              .width(5)
              .height(-5)
              .bitrate(5000)
              .preset("fast")
              .build();
      mockMvc
          .perform(
              post("/outputProfile/config")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(invalidDto)))
          .andExpect(status().isBadRequest());
    }
  }

  @Nested
  @DisplayName("GET /outputProfile/config/{configName}")
  class GetConfigByConfigNameTests {

    @Test
    @DisplayName("Should return 200 OK with object when config exists")
    void getConfig_Success() throws Exception {
      when(service.getOutputProfileByConfigName("hd_profile"))
          .thenReturn(mapper.convertToGetDTO(sampleProfile));

      mockMvc
          .perform(get("/outputProfile/config/{configName}", "hd_profile"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.configName").value("hd_profile"))
          .andExpect(jsonPath("$.bitrate").value(5000));
    }

    @Test
    @DisplayName("Should pass through 404 Status if service layer throws NotFoundException")
    void getConfig_NotFound() throws Exception {
      when(service.getOutputProfileByConfigName("missing"))
          .thenThrow(new OutputProfileServiceException(HttpStatus.NOT_FOUND, "Not found"));

      mockMvc
          .perform(get("/outputProfile/config/{configName}", "missing"))
          .andExpect(status().isNotFound());
    }
  }

  @Nested
  @DisplayName("POST /outputProfile/config")
  class CreateNewConfigTests {

    @Test
    @DisplayName("Should return 201 Created and return the object when validation succeeds")
    void createConfig_Success() throws Exception {
      when(service.saveOutputProfileConfig(any(LCOutputProfileCreateDTO.class)))
          .thenReturn(defaultCreateDto);

      mockMvc
          .perform(
              post("/outputProfile/config")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(defaultCreateDto)))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.configName").value("hd_profile"));
    }

    @Test
    @DisplayName("Should pass through 409 Conflict if service throws a duplicate name exception")
    void createConfig_Conflict() throws Exception {
      when(service.saveOutputProfileConfig(any(LCOutputProfileCreateDTO.class)))
          .thenThrow(new OutputProfileServiceException(HttpStatus.CONFLICT, "Already exists"));

      mockMvc
          .perform(
              post("/outputProfile/config")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(defaultCreateDto)))
          .andExpect(status().isConflict());
    }
  }

  @Nested
  @DisplayName("DELETE /outputProfile/config/{configName}")
  class DeleteConfigTests {

    @Test
    @DisplayName("Should return 200 OK upon successful deletion context execution")
    void deleteConfig_Success() throws Exception {
      doNothing().when(service).deleteOutputProfileConfig("hd_profile");

      mockMvc
          .perform(delete("/outputProfile/config/{configName}", "hd_profile"))
          .andExpect(status().isOk());

      verify(service, times(1)).deleteOutputProfileConfig("hd_profile");
    }

    @Test
    @DisplayName("Should return 404 Not Found if target configuration doesn't exist")
    void deleteConfig_NotFound() throws Exception {
      doThrow(new OutputProfileServiceException(HttpStatus.NOT_FOUND, "Doesn't exist"))
          .when(service)
          .deleteOutputProfileConfig("missing");

      mockMvc
          .perform(delete("/outputProfile/config/{configName}", "missing"))
          .andExpect(status().isNotFound());
    }
  }

  @Nested
  @DisplayName("PATCH /outputProfile/config/{configName}")
  class UpdateOutputProfileTests {

    @Test
    @DisplayName("Should return 200 OK and payload details when partial fields patch successfully")
    void patchConfig_Success() throws Exception {
      LCOutputProfileCreateDTO updateDto =
          LCOutputProfileCreateDTO.builder().videoCodec("av1").bitrate(8000).build();

      LCOutputProfileUpdateDTO updatedProfile =
          LCOutputProfileUpdateDTO.builder()
              .configName("hd_profile")
              .videoCodec("av1")
              .bitrate(8000)
              .build();

      when(service.partialUpdateOutputProfileConfig(
              eq("hd_profile"), any(LCOutputProfileUpdateDTO.class)))
          .thenReturn(updatedProfile);

      mockMvc
          .perform(
              patch("/outputProfile/config/{configName}", "hd_profile")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(updateDto)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.videoCodec").value("av1"))
          .andExpect(jsonPath("$.bitrate").value(8000));
    }

    @Test
    @DisplayName("Should return 400 Bad Request when JSON structure is malformed syntax")
    void patchConfig_MalformedJsonSyntax() throws Exception {
      String malformedJson = "{ \"videoCodec\": \"av1\", \"width\": ";

      mockMvc
          .perform(
              patch("/outputProfile/config/{configName}", "hd_profile")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(malformedJson))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.error").value("Bad Request"))
          .andExpect(
              jsonPath("$.message")
                  .value(
                      "Malformed JSON request body. Ensure that numeric fields (like width, height, or bitrate) are made of digits."));
    }

    @Test
    @DisplayName("Should return 400 Bad Request when text is provided for a numeric field")
    void patchConfig_InvalidFieldDataType() throws Exception {
      String badDataTypeJson = "{ \"width\": \"invalid_string_instead_of_number\" }";

      mockMvc
          .perform(
              patch("/outputProfile/config/{configName}", "hd_profile")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(badDataTypeJson))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.details").exists());
    }
  }
}
