/* Copyright 2026 UnitedCloud. All rights reserved. */
package uc.orchestrator.service;

public record StreamerCandidate(
    Long streamerId, String streamerName, int activeSessions, int capacity, int port) {}
