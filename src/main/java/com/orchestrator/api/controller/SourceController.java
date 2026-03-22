package com.orchestrator.api.controller;

import com.orchestrator.api.dto.SourceRequest;
import com.orchestrator.api.dto.SourceResponse;
import com.orchestrator.api.service.SourceService;
import com.orchestrator.domain.entity.Source;
import com.orchestrator.domain.repository.SourceRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
@RestController
@RequestMapping("/api/sources")
@RequiredArgsConstructor
public class SourceController {

    private final SourceService sourceService;
    private final SourceRepository sourceRepository;
    
    @GetMapping
    public List<SourceResponse> getAllSources() {
        return sourceService.getAllSources().stream()
                .map(SourceResponse::fromEntity)
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<SourceResponse> getSourceById(@PathVariable UUID id) {
        return sourceService.getSourceById(id)
                .map(source -> ResponseEntity.ok(SourceResponse.fromEntity(source)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<SourceResponse> createSource(@Valid @RequestBody SourceRequest request) {
        Source source = Source.builder()
                .name(request.getName())
                .type(request.getType())
                .config(request.getConfig())
                .enabled(request.getEnabled() != null ? request.getEnabled() : false)
                .build();
        
        Source created = sourceService.createSource(source);
        return ResponseEntity.ok(SourceResponse.fromEntity(created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SourceResponse> updateSource(
            @PathVariable UUID id,
            @Valid @RequestBody SourceRequest request) {
        
        return sourceRepository.findById(id)
                .map(existing -> {
                    existing.setName(request.getName());
                    existing.setConfig(request.getConfig());
                    existing.setEnabled(request.getEnabled());
                    Source updated = sourceService.updateSource(id, existing);
                    return ResponseEntity.ok(SourceResponse.fromEntity(updated));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSource(@PathVariable UUID id) {
        sourceService.deleteSource(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/sync")
    public ResponseEntity<Void> triggerSync(@PathVariable UUID id) {
        sourceService.triggerSync(id);
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/{id}/enable-cdc")
    public ResponseEntity<Void> enableCdc(@PathVariable UUID id) {
        sourceService.enableCdc(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/disable-cdc")
    public ResponseEntity<Void> disableCdc(@PathVariable UUID id) {
        sourceService.disableCdc(id);
        return ResponseEntity.noContent().build();
    }
}