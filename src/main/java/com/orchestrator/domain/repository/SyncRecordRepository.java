package com.orchestrator.domain.repository;

import com.orchestrator.domain.entity.SyncRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SyncRecordRepository extends JpaRepository<SyncRecord, UUID> {
    Optional<SyncRecord> findBySourceIdAndExternalId(UUID sourceId, String externalId);

    long countBySourceId(UUID sourceId);
}