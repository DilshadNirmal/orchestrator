package com.orchestrator.domain.entity;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "sync_records", uniqueConstraints = @UniqueConstraint(columnNames = {"source_id", "external_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SyncRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_id", nullable = false)
    private Source source;

    @Column(name = "external_id", nullable = false)
    private String externalId;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> data;

    @Column
    private String operation;

    @Column(name = "synced_at")
    private Instant syncedAt;

    @PrePersist
    protected void onCreate() {
        if (syncedAt == null) {
            syncedAt = Instant.now();
        }
    }
}