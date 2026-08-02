/* Copyright 2026 UnitedCloud. All rights reserved. */
package uc.orchestrator.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfigBean {

  @Bean
  public ObjectMapper objectMapper() {
    return new ObjectMapper();
  }
}
