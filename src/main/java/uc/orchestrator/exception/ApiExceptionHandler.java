/* Copyright 2026 UnitedCloud. All rights reserved. */
package uc.orchestrator.exception;

import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import uc.orchestrator.constants.StringLiterals;

@Slf4j
@RestControllerAdvice
public class ApiExceptionHandler {

  private Map<String, Object> createErrorBody(
      String message, String timestamp, int status, String reason) {

    Map<String, Object> body = new LinkedHashMap<>();
    body.put(StringLiterals.MESSAGE, message);
    body.put(StringLiterals.TIMESTAMP, timestamp);
    body.put(StringLiterals.STATUS, status);
    body.put(StringLiterals.ERROR, reason);

    return body;
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Object> handleValidationExceptions(MethodArgumentNotValidException ex) {
    Map<String, Object> errors = new HashMap<>();

    ex.getBindingResult()
        .getFieldErrors()
        .forEach(
            error -> {
              String fieldName = error.getField();
              String errorMessage = error.getDefaultMessage();
              errors.put(fieldName, errorMessage);
              errors.put(StringLiterals.TIMESTAMP, Instant.now().toString());
              errors.put(StringLiterals.STATUS, HttpStatus.BAD_REQUEST.value());
              errors.put(StringLiterals.ERROR, HttpStatus.BAD_REQUEST.getReasonPhrase());
              log.warn("Validation failed: Field '{}' - {}", fieldName, errorMessage);
            });

    return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(OutputProfileServiceException.class)
  public ResponseEntity<Object> handleOutputProfileServiceException(
      OutputProfileServiceException ex) {
    log.error("Service exception intercepted: {} - Status: {}", ex.getMessage(), ex.getStatus());

    Map<String, Object> body =
        this.createErrorBody(
            ex.getMessage(),
            Instant.now().toString(),
            ex.getStatus().value(),
            ex.getStatus().getReasonPhrase());

    return new ResponseEntity<>(body, ex.getStatus());
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<Object> handleHttpMessageNotReadableException(
      HttpMessageNotReadableException ex) {

    log.error("JSON parsing failed: {}", ex.getMessage());

    String clientMessage =
        "Malformed JSON request body. Ensure that numeric fields (like width, height, or bitrate) are made of digits.";

    Map<String, Object> body =
        this.createErrorBody(
            clientMessage,
            Instant.now().toString(),
            HttpStatus.BAD_REQUEST.value(),
            HttpStatus.BAD_REQUEST.getReasonPhrase());

    if (ex.getCause() != null) {
      body.put("details", ex.getCause().getMessage());
    }

    return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(StreamerException.class)
  public ResponseEntity<Object> handleStreamerException(StreamerException ex) {

    log.error(
        "Service exception (Streamer API) intercepted: {} - Status: {}",
        ex.getMessage(),
        ex.getStatus());

    Map<String, Object> body =
        this.createErrorBody(
            ex.getMessage(),
            Instant.now().toString(),
            ex.getStatus().value(),
            ex.getStatus().getReasonPhrase());

    return new ResponseEntity<>(body, ex.getStatus());
  }

  @ExceptionHandler(MappingConfigurationException.class)
  public ResponseEntity<Object> handleMappingConfigurationException(
      MappingConfigurationException ex) {

    log.error("Mapping LC configuration failed: {}", ex.getMessage());

    Map<String, Object> body =
        this.createErrorBody(
            ex.getMessage(),
            Instant.now().toString(),
            ex.getStatus().value(),
            ex.getStatus().getReasonPhrase());
    if (ex.getCause() != null) {
      body.put("details", ex.getCause().getMessage());
    }

    return new ResponseEntity<>(body, ex.getStatus());
  }

  @ExceptionHandler(NoStreamerAvailableException.class)
  public ResponseEntity<Object> handleNoStreamerAvailableException(
      NoStreamerAvailableException ex) {

    log.error("No streamer available: {}", ex.getMessage());

    Map<String, Object> body =
        this.createErrorBody(
            ex.getMessage(),
            Instant.now().toString(),
            ex.getStatus().value(),
            ex.getStatus().getReasonPhrase());

    return new ResponseEntity<>(body, ex.getStatus());
  }

  @ExceptionHandler(VodValidationException.class)
  public ResponseEntity<Object> handleVodValidationException(VodValidationException ex) {

    log.error("VOD validation failed: {}", ex.getMessage());

    Map<String, Object> body =
        createErrorBody(
            ex.getMessage(),
            Instant.now().toString(),
            ex.getStatus().value(),
            ex.getStatus().getReasonPhrase());

    return new ResponseEntity<>(body, ex.getStatus());
  }

  @ExceptionHandler(ProcessExecutionException.class)
  public ResponseEntity<Object> handleProcessExecutionException(ProcessExecutionException ex) {

    log.error("Process execution failed: {}", ex.getMessage());

    Map<String, Object> body =
        createErrorBody(
            ex.getMessage(),
            Instant.now().toString(),
            ex.getStatus().value(),
            ex.getStatus().getReasonPhrase());

    return new ResponseEntity<>(body, ex.getStatus());
  }
}
