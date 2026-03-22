package com.orchestrator.core.orchestrator;

import com.orchestrator.cdc.DebeziumService;
import com.orchestrator.domain.entity.Job;
import com.orchestrator.domain.entity.Source;
import com.orchestrator.domain.repository.JobRepository;
import com.orchestrator.domain.repository.SourceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SyncOrchestrator {
    
    private final SourceRepository sourceRepository;
    private final JobRepository jobRepository;
    private final DebeziumService debeziumService;

    @Transactional
    public Job triggerManualSync(Source source) {
        log.info("Triggering manual sync for source: {}", source.getName());

        Job job = Job.builder().source(source).status(Job.JobStatus.PENDING).operation("MANUAL").startedAt(Instant.now()).build();

        job = jobRepository.save(job);

        try {
            job.setStatus(Job.JobStatus.RUNNING);
            job = jobRepository.save(job);
            // Start CDC temporarily for manual sync
            debeziumService.startCdc(source);
            // In a real implementation, you would:
            // 1. Fetch data from MongoDB directly
            // 2. Transform data
            // 3. Save to PostgreSQL
            // 4. Stop CDC after sync
            job.markCompleted();
            job.setRecordsProcessed(0);
            job.setRecordsFailed(0);
        } catch (Exception e) {
            log.error("Error during manual sync", e);
            job.markFailed(e.getMessage());
        }

        return jobRepository.save(job);
    }

     public void enableCdc(UUID sourceId) {
        sourceRepository.findById(sourceId).ifPresent(source -> {
            source.setEnabled(true);
            source.setCdcActive(true);
            sourceRepository.save(source);
            debeziumService.startCdc(source);
            log.info("Enabled CDC for source: {}", source.getName());
        });
    }
    public void disableCdc(UUID sourceId) {
        sourceRepository.findById(sourceId).ifPresent(source -> {
            source.setEnabled(false);
            debeziumService.stopCdc(source);
            log.info("Disabled CDC for source: {}", source.getName());
        });
    }
}
