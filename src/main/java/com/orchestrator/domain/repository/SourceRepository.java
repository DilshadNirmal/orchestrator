package com.orchestrator.domain.repository;

import com.orchestrator.domain.entity.Source;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SourceRepository extends JpaRepository<Source, UUID> {
    List<Source> findByEnabledTrue();
    List<Source> findByCdcActiveTrue();
}