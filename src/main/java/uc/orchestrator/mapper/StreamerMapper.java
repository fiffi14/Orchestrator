/* Copyright 2026 UnitedCloud. All rights reserved. */
package uc.orchestrator.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import uc.orchestrator.dto.StreamerRegistrationRequestDTO;
import uc.orchestrator.dto.StreamerResponseDTO;
import uc.orchestrator.dto.StreamerUpdateRequestDTO;
import uc.orchestrator.entity.StreamerInfo;

@Mapper(componentModel = "spring")
public interface StreamerMapper {

  @Mapping(target = "streamerId", ignore = true)
  @Mapping(target = "lastHeartbeatTimestamp", ignore = true)
  StreamerInfo toEntity(StreamerRegistrationRequestDTO request);

  @Mapping(target = "online", ignore = true)
  StreamerResponseDTO toResponseDTO(StreamerInfo entity);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  void updateEntityFromDto(StreamerUpdateRequestDTO dto, @MappingTarget StreamerInfo entity);
}
