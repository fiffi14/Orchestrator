/* Copyright 2026 UnitedCloud. All rights reserved. */
package uc.orchestrator.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import uc.orchestrator.kafka.streamer.StreamerCapacityEvent;

@Service
public class StreamerCapacityConsumer {

  private static final Logger logger = LoggerFactory.getLogger(StreamerCapacityConsumer.class);

  private final StreamerStateService streamerStateService;

  public StreamerCapacityConsumer(StreamerStateService streamerStateService) {
    this.streamerStateService = streamerStateService;
  }

  @KafkaListener(topics = KafkaTopics.STREAMER_CAPACITY_TOPIC)
  public void consume(StreamerCapacityEvent event) {

    logger.info(
        "Received capacity event: streamerId={}, activeSessions={}",
        event.getStreamerName(),
        event.getActiveSessions());

    streamerStateService.update(
        event.getStreamerName(), event.getActiveSessions(), event.getTimestamp());

    var state = streamerStateService.get(event.getStreamerName());

    logger.info("STATE AFTER UPDATE = {}", state != null ? state : "NOT_FOUND");
  }
}
