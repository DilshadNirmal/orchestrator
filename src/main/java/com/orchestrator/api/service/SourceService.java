package com.orchestrator.api.service;

import com.orchestrator.core.orchestrator.SyncOrchestrator;
import com.orchestrator.domain.entity.Job;
import com.orchestrator.domain.entity.Source;
import com.orchestrator.domain.repository.SourceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SourceService {
    
    private final SourceRepository sourceRepository;
    private final SyncOrchestrator syncOrchestrator;

    public List<Source> getAllSources() {
        return sourceRepository.findAll();
    }

    public Optional<Source> getSourceById(UUID id) {
        return sourceRepository.findById(id);
    }

    public List<Source> getEnabledSources() {
        return sourceRepository.findByEnabledTrue();
    }

    @Transactional
    public Source createSource(Source source) {
        log.info("Creating new source: {}", source.getName());
        return sourceRepository.save(source);
    }

    @Transactional
    public Source updateSource(UUID id, Source updated) {
        return sourceRepository.findById(id).map(existing -> {
            existing.setName(updated.getName());
            existing.setType(updated.getType());
            existing.setConfig(updated.getConfig());
            existing.setTransformRules(updated.getTransformRules());
            existing.setEnabled(updated.getEnabled());
            return sourceRepository.save(existing);
        })
        .orElseThrow(() -> new RuntimeException("Source not found: " + id));
    }

     @Transactional
    public void deleteSource(UUID id) {
        // Stop CDC first if running
        syncOrchestrator.disableCdc(id);
        sourceRepository.deleteById(id);
        log.info("Deleted source: {}", id);
    }

    public Job triggerSync(UUID sourceId) {
        return sourceRepository.findById(sourceId)
                .map(source -> syncOrchestrator.triggerManualSync(source))
                .orElseThrow(() -> new RuntimeException("Source not found: " + sourceId));
    }

    public void enableCdc(UUID sourceId) {
        syncOrchestrator.enableCdc(sourceId);
    }
    
    public void disableCdc(UUID sourceId) {
        syncOrchestrator.disableCdc(sourceId);
    }
}
