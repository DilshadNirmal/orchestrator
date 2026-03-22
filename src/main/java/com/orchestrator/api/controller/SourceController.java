package com.orchestrator.api.controller;

import com.orchestrator.domain.entity.Source;
import com.orchestrator.domain.repository.SourceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/sources")
@RequiredArgsConstructor
public class SourceController {
    private final SourceRepository sourceRepository;

    @GetMapping
    public List<Source> getAllSources() {
        return sourceRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Source> getSourceById(@PathVariable UUID id) {
        return sourceRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/enabled")
    public List<Source> getEnabledSources() {
        return sourceRepository.findByEnabledTrue();
    }

    @PostMapping
    public Source createSource(@RequestBody Source source) {
        return sourceRepository.save(source);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Source> updateSource(@PathVariable UUID id, @RequestBody Source updated) {
        return sourceRepository.findById(id).map(existing -> {
            existing.setName(updated.getName());
            existing.setConfig(updated.getConfig());
            existing.setTransformRules(updated.getTransformRules());
            existing.setEnabled(updated.getEnabled());
            return ResponseEntity.ok(sourceRepository.save(existing));
        })
        .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSource(@PathVariable UUID id) {
        sourceRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}