/* Copyright 2026 UnitedCloud. All rights reserved. */
package uc.orchestrator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class OrchestratorApplication {

  public static void main(String[] args) {
    SpringApplication.run(OrchestratorApplication.class, args);
  }
}
