/* Copyright 2026 UnitedCloud. All rights reserved. */
package uc.orchestrator.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import uc.orchestrator.dto.LCOutputProfileCreateDTO;
import uc.orchestrator.dto.LCOutputProfileGetDTO;
import uc.orchestrator.dto.LCOutputProfileUpdateDTO;
import uc.orchestrator.entity.LCOutputProfile;

@Mapper(componentModel = "spring")
public interface LCOutputProfileMapper {

  LCOutputProfileGetDTO convertToGetDTO(LCOutputProfile entity);

  LCOutputProfileCreateDTO convertToCreateDTO(LCOutputProfile entity);

  LCOutputProfileUpdateDTO convertToUpdateDTO(LCOutputProfile entity);

  LCOutputProfile convertToEntity(LCOutputProfileCreateDTO dto);

  LCOutputProfile convertToEntity(LCOutputProfileGetDTO dto);

  LCOutputProfile convertToEntity(LCOutputProfileUpdateDTO dto);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  void updateEntityFromDto(LCOutputProfileUpdateDTO dto, @MappingTarget LCOutputProfile entity);
}
