/* Copyright 2026 UnitedCloud. All rights reserved. */
package uc.orchestrator.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uc.orchestrator.entity.LCOutputProfile;

/*
 * by extending JpaRepository we have inherited some of the CRUD methods
 * and Spring does parsing the method names: CRUD_method + Entity_field => based on which it makes the resulting SQL query
 *
 * the second part of the method needs to be EXACTLY NAMED AS ENTITY CLASS FIELD's NAME
 *
 * for more dynamic and complex filtering:
 * QueryDslPredicateExecutor (external library -> dependency)
 * JpaSpecificationExecutor (part of Spring Data JPA)
 *
 * */

@Repository
public interface LCOutputProfileRepository extends JpaRepository<LCOutputProfile, Long> {
  Optional<LCOutputProfile> findByConfigName(String configName);

  boolean existsByConfigName(String configName);

  void deleteByConfigName(String configName);

  List<LCOutputProfile> findAllByIdIn(List<Long> ids);
}
