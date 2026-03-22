package com.orchestrator.cdc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.orchestrator.config.RabbitMQConfig;
import com.orchestrator.domain.entity.Source;
import com.orchestrator.messaging.SyncEvent;
import com.orchestrator.domain.repository.SourceRepository;
import io.debezium.config.Configuration;
import io.debezium.engine.ChangeEvent;
import io.debezium.engine.DebeziumEngine;
import io.debezium.engine.format.Json;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class DebeziumService {
    
    private final SourceRepository sourceRepository;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    private final Map<UUID, DebeziumEngine<ChangeEvent<String, String>>> runningEngines = new ConcurrentHashMap<>();
    private final ExecutorService executor = Executors.newCachedThreadPool();

    @PostConstruct
    public void init() {
        log.info("DebeziumService initialized");
        // start cdc for all sources with cdc_active = true
        startActiveCdcSources();
    }

    @Scheduled(fixedDelay= 60000)
    public void startActiveCdcSources() {
        List<Source> activeSources = sourceRepository.findByCdcActiveTrue();
        for (Source source : activeSources) {
            if (!runningEngines.containsKey(source.getId())) {
                startCdc(source);
            }
        }
    }

    public void startCdc(Source source) {
        if (runningEngines.containKey(source.getId())) {
            log.warn("CDC already running for source: {}", source.getName());
            return;
        }

        Map<String, Object> config = source.getConfig();
        String mongodbUri = (String) config.get("mongodbUri");
        String database = (String) config.get("database");
        String collection = (String) config.get("collection");

        Configuration debeziumConfig = Configuration.create()
                            .with("name", "orchestrator-" + source.getId())
                            .with("connector.class", "io.debezium.connector.mongodb.MongoDbConnector")
                            .with("mongodb.connection.string", mongodbUri)
                            .with("mongodb.include.list", database + "." + collection)
                            .with("topic.prefix", "orchestrator-" + source.getId())
                            .with("snapshot.mode", "initial")
                            .with("offset.storage", "org.apache.kafka.connect.storage.FileOffsetBackingStore")
                            .with("offset.storage.file.filename", ".debezium/offsets-" + source.getId() + ".dat")
                            .with("offset.flush.interval.ms", "1000")
                            .with("schema.history.internal", "io.debezium.storage.file.history.FileSchemaHistory")
                            .with("schema.history,internal.file.filename", ".debezium/history-" + source.getId() + ".dat")
                            .build();

        DebeziumEngine<ChangeEvent<String, String>> engine = DebeziumEngine.create(Json.class)
                .using(debeziumConfig.asProperties())
                .notifying(this::handleChangeEvent)
                .using((success, message, error) -> {
                    if (!success) {
                        log.error("Debezium engine stopped with error: {}", message, error);
                    }
                })
                .build();

        runningEngines.put(source.getId(), engine);
        executor.submit(engine);

        // update source status
        source.setCdcActive(true);
        sourceRepository.save(source);

        log.info("Started CDC for source: {}", source.getName());
    }

    public void stopCdc(Source source) {
        DebeziumEngine<ChangeEvent<String, String>> engine = runningEngines.remove(source.getId());

        if (engine != null) {
            try {
                engine.close();
                source.setCdcActive(false);
                sourceRepository.save(source);
                log.info("Stopped CDC for source: {}", source.getName());
            } catch (Exception e) {
                 log.error("Error stopping CDC for source: {}", source.getName(), e);
            }
        }
    }

    public void handleChangeEvent(ChangeEvent<String, String> event) {
        try {
            JsonNode payload = objectMapper.readTree(event.value());

            if (payload.has("op")) {
                String operation = payload.get("op").asText();

                String externalId = extractId(payload);

                Map<String, Object> data = extractData(payload, operation);

                SyncEvent syncEvent = new SyncEvent();
                syncEvent.setSourceId(extractSourceId(event.key()));
                syncEvent.setExternalId(externalId);
                syncEvent.setOperation(operation);
                syncEvent.setData(data);
                syncEvent.setTimestamp(Instant.now());
                syncEvent.setMetaData(Map.of(
                    "collection", extractCollection(event.key()),
                    "database", extractDatabase(event.key())
                ));

                // send to RabbitMQ
                rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE_NAME,
                    "sync." + operation.toLowerCase(),
                    syncEvent
                );

                log.debug("published sync event: {}", syncEvent);
            } 
        } catch (Exception e) {
                log.error("Error handling change event", e);
        }
    }

    private String extractId(ChangeEvent<String, String> event) {
        try {
            JsonNode key = objectMapper.readTree(event.key());
            return key.get("_id").toString();
        } catch (Exception e) {
            return UUID.randomUUID().toString();
        }
    }

    private Map<String, Object> extractData(JsonNode payload, String operation) {
        Map<String, Object> data = new HashMap<>();
        if (payload.has("after")) {
            data = objectMapper.convertValue(payload.get("after"), Map.class);
        } else if (payload.has("patch")) {
            // Handle MongoDB oplog patch format
        }
        return data;
    }

    private UUID extractSourceId(String key) {
        return UUID.fromString(key.split(":")[0]);
    }

    private String extractCollection(String key) {
        return key.split("\\.")[1];
    }

    private String extractDatabase(String key) {
        return key.split("\\.")[0];
    }

    @PreDestroy
    public void shutdown() {
        log.info("Shutting Down Debezium CDC engines");
        runningEngines.keySet().forEach(sourceId -> {
            sourceRepository.findById(sourceId).ifPresent(this::stopCdc);
        });
        executor.shutdown();
    }
}
