package com.orchestrator.domain.entity;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
@TABLE(name = "sources")
@Getter
@Setter
@NoArgsContructor
@ALLArgsConstructor
@Builder
public class Source {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUDI id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String type;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> config;

    @Type(JsonType.class)
    @Column(name = "transform_rules", columnDefinition = "jsonb")
    private List<TransformRule> transformRules;

    @Column(nullable = false)
    @Builder.Default
    private Boolean enabled = false

    @Column(name = "cdc_active")
    @Builder.Default
    private Boolean cdcActive = false;

    @Column(name = "last_sync_at")
    private Instant lastSyncAt;
    
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TransformRule {
        private String field;
        private String sourceField;
        private String type;
        private Map<String, String> mapValues;
    }
}