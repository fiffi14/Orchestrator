/* Copyright 2026 UnitedCloud. All rights reserved. */
package uc.orchestrator.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.api.ErrorMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uc.orchestrator.dto.LCOutputProfileCreateDTO;
import uc.orchestrator.dto.LCOutputProfileGetDTO;
import uc.orchestrator.dto.LCOutputProfileUpdateDTO;
import uc.orchestrator.service.LCOutputProfileService;

@Slf4j
@RestController
@RequestMapping("api/v1/outputProfile")
@RequiredArgsConstructor
public class LCOutputProfileController {
  private final LCOutputProfileService service;

  @Operation(
      summary = "Retrieve all output profile configurations.",
      description =
          "This endpoint returns a list of all active multimedia transcoding configurations stored in the system.",
      parameters = {
        @Parameter(
            name = "Get-Request",
            description = "Optional header for tracing requests through the system.",
            in = ParameterIn.HEADER,
            schema = @Schema(type = "string"))
      },
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved the list of configurations.",
            headers = {
              @Header(
                  name = "Get-Request",
                  description = "Optional tracing header returned if it was provided.")
            },
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = LCOutputProfileGetDTO.class)))
      })
  @GetMapping
  public ResponseEntity<List<LCOutputProfileGetDTO>>
      getAllConfigs() { // with ResponseEntity we wrap the response around HTTP not sending it raw
    List<LCOutputProfileGetDTO> configs = service.getAllOutputProfileConfigs();

    log.info("Retrieved {} configurations.", configs.size());
    return ResponseEntity.ok(configs);
  }

  @Operation(
      summary = "Retrieve output profile details by configuration name.",
      description =
          "Returns detailed metadata matching a specific configuration name. Throws 404 if the profile does not exist.",
      parameters = {
        @Parameter(
            name = "Get-Request",
            description = "Optional header for tracing requests through the system.",
            in = ParameterIn.HEADER,
            schema = @Schema(type = "string"))
      },
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved profile details.",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = LCOutputProfileGetDTO.class))),
        @ApiResponse(
            responseCode = "404",
            description = "Not Found - The configuration profile name was not found.",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorMessage.class)))
      })
  @GetMapping("/{configName}")
  public ResponseEntity<LCOutputProfileGetDTO> getConfigByConfigName(
      @PathVariable String configName) {
    log.info("Fetching configuration details for: {}", configName);
    LCOutputProfileGetDTO config = service.getOutputProfileByConfigName(configName);
    return ResponseEntity.ok(config);
  }

  @Operation(
      summary = "Create a new transcoding profile configuration.",
      description =
          "Saves a new video/audio transcoding profile mapping rule. The profile name must be globally unique.",
      parameters = {
        @Parameter(
            name = "Post-Request",
            description = "Optional header for tracing requests through the system.",
            in = ParameterIn.HEADER,
            schema = @Schema(type = "string"))
      },
      requestBody =
          @io.swagger.v3.oas.annotations.parameters.RequestBody(
              description = "Create configuration profile payload parameters",
              required = true,
              content =
                  @Content(
                      mediaType = MediaType.APPLICATION_JSON_VALUE,
                      schema = @Schema(implementation = LCOutputProfileCreateDTO.class))),
      responses = {
        @ApiResponse(
            responseCode = "201",
            description = "Successfully created the configuration profile.",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = LCOutputProfileGetDTO.class))),
        @ApiResponse(
            responseCode = "409",
            description = "Conflict - A profile configuration with this name already exists.",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorMessage.class)))
      })
  @PostMapping
  public ResponseEntity<LCOutputProfileCreateDTO> createNewConfig(
      @Valid @RequestBody LCOutputProfileCreateDTO requestDTO) {
    LCOutputProfileCreateDTO data = service.saveOutputProfileConfig(requestDTO);

    log.info("Successfully saved new configuration: {}", requestDTO.getConfigName());
    return ResponseEntity.status(HttpStatus.CREATED).body(data);
  }

  @Operation(
      summary = "Delete an output profile configuration by name.",
      description = "Removes a target configuration profile entirely from the processing systems.",
      parameters = {
        @Parameter(
            name = "Delete-Request",
            description = "Optional header for tracing requests through the system.",
            in = ParameterIn.HEADER,
            schema = @Schema(type = "string"))
      },
      responses = {
        @ApiResponse(responseCode = "200", description = "Successfully deleted specified profile."),
        @ApiResponse(
            responseCode = "404",
            description = "Not Found - Configuration does not exist.",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorMessage.class)))
      })
  @DeleteMapping("/{configName}")
  public ResponseEntity<Void> deleteConfig(@PathVariable String configName) {
    service.deleteOutputProfileConfig(configName);

    log.info("Successfully deleted the configuration: {}", configName);
    return ResponseEntity.ok().build();
  }

  @Operation(
      summary = "Partially update an existing transcoding configuration.",
      description =
          "Applies a delta modification payload to an existing profile. Only fields supplied inside the request will be merged.",
      parameters = {
        @Parameter(
            name = "Patch-Request",
            description = "Optional header for tracing requests through the system.",
            in = ParameterIn.HEADER,
            schema = @Schema(type = "string"))
      },
      requestBody =
          @io.swagger.v3.oas.annotations.parameters.RequestBody(
              description = "Partial fields patch map",
              required = true,
              content =
                  @Content(
                      mediaType = MediaType.APPLICATION_JSON_VALUE,
                      schema = @Schema(implementation = LCOutputProfileUpdateDTO.class))),
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully patched profile attributes.",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = LCOutputProfileUpdateDTO.class))),
        @ApiResponse(
            responseCode = "404",
            description = "Not Found - Profile name targeted for patch does not exist.",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorMessage.class)))
      })
  @PatchMapping("/{configName}")
  public ResponseEntity<LCOutputProfileUpdateDTO> updateOutputProfile(
      @PathVariable String configName, @Valid @RequestBody LCOutputProfileUpdateDTO dto) {
    LCOutputProfileUpdateDTO updatedProfile =
        service.partialUpdateOutputProfileConfig(configName, dto);

    log.info("Successfully updated the configuration: {}", configName);
    return ResponseEntity.ok(updatedProfile);
  }
}
