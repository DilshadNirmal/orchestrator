package com.orchestrator.api.controller;

import com.orchestrator.domain.entity.Job;
import com.orchestrator.domain.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobRepository jobRepository;

    @GetMapping
    public Page<Job> getAllJobs(@PageableDefault(size = 20, sort = "startedAt") Pageable pageable) {
        return jobRepository.findAllByOrderByStartedAtDesc(pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Job> getJobById(@PathVariable UUID id) {
        return jobRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/source/{sourceId}")
    public Page<Job> getJobsBySource(@PathVariable UUID sourceId, @PageableDefault(size = 20) Pageable pageable) {
        return jobRepository.findBySourceIdOrderByStartedAtDesc(sourceId, pageable);
    }
}