package com.orchestrator.messaging;

import lombok.Data;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Data
public class SyncEvent {
    private UUID sourceId;
    private String externalId;
    private String operation;
    private Map<String, Object> data;
    private Instant timestamp;
    private Map<String, Object> metadata;
}
