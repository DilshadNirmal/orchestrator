package com.orchestrator.messaging;

import com.orchestrator.config.RabbitMQConfig;
import com.orchestrator.domain.entity.Job;
import com.orchestrator.domain.entity.Source;
import com.orchestrator.domain.entity.SyncRecord;
import com.orchestrator.domain.repository.JobRepository;
import com.orchestrator.domain.repository.SourceRepository;
import com.orchestrator.domain.repository.SyncRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class SyncEventListener {
    private final SourceRepository sourceRepository;
    private final SyncRecordRepository syncRecordRepository;
    private final JobRepository jobRepository;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    @Transactional
    public void handleSyncEvent(SyncEvent event) {
        log.info("Received sync event: sourceId={}, externalId={}, operation={}", event.getSourceId(), event.getExternalId(), event.getOperation());

        try {
            Optional<Source> sourceOpt = sourceRepository.findById(event.getSourceId());
            if (sourceOpt.isEmpty()) {
                log.warn("Source not found: {}", event.getSourceId());
                return;
            }

            Source source = sourceOpt.get();

            switch (event.getOperation()) {
                case "INSERT", "UPDATE" -> upsertRecord(source, event);
                case "DELETE" -> deleteRecord(source, event);
                default -> log.warn("Unknown Operation: {}", event.getOperation());
            }

            updateJobStats(source, event);
        } catch (Exception e) {
            log.error("Error processing sync event", e);
        }

    }

    private void upsertRecord(Source source, SyncEvent event) {

        Optional<SyncRecord> existing = syncRecordRepository
                .findBySourceIdAndExternalId(source.getId(), event.getExternalId());

        if (existing.isPresent()) {
            SyncRecord record = existing.get();
            record.setData(event.getData());
            record.setOperation(event.getOperation());
            syncRecordRepository.save(record);
            log.debug("Updated record: {}", event.getExternalId());
        } else {
            SyncRecord record = SyncRecord.builder()
                    .source(source)
                    .externalId(event.getExternalId())
                    .data(event.getData())
                    .operation(event.getOperation())
                    .build();
            syncRecordRepository.save(record);
            log.debug("Inserted new record: {}", event.getExternalId());
        }

    }

    private void deleteRecord(Source source, SyncEvent event) {
        syncRecordRepository.findBySourceIdAndExternalId(source.getId(), event.getExternalId())
            .ifPresent(record -> {
                syncRecordRepository.delete(record);
                log.debug("Deleted record: {}", event.getExternalId());
            });
    }

    private void updateJobStats(Source source, SyncEvent event) {
        // Find the latest running job for this source and increment stats
        // Implementation depends on your job tracking strategy
    }
}
