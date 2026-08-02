/* Copyright 2026 UnitedCloud. All rights reserved. */
package uc.orchestrator.config;

import java.time.Instant;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InstantProvider {

  @Bean
  public Instant now() {
    return Instant.now();
  }
}
