package com.orchestrator.api.dto;

import com.orchestrator.domain.entity.Source;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
public class SourceResponse {
    private UUID id;
    private String name;
    private String type;
    private Map<String, Object> config;
    private List<TransformRuleResponse> transformRules;
    private Boolean enabled;
    private Boolean cdcActive;
    private Instant lastSyncAt;
    private Instant createdAt;
    private Instant updatedAt;

    public static SourceResponse fromEntity(Source source) {
         return SourceResponse.builder()
                .id(source.getId())
                .name(source.getName())
                .type(source.getType())
                .config(source.getConfig())
                .transformRules(source.getTransformRules() != null ?
                        source.getTransformRules().stream()
                                .map(tr -> TransformRuleResponse.builder()
                                        .field(tr.getField())
                                        .sourceField(tr.getSourceField())
                                        .type(tr.getType())
                                        .mapValues(tr.getMapValues())
                                        .build())
                                .toList() : List.of())
                .enabled(source.getEnabled())
                .cdcActive(source.getCdcActive())
                .lastSyncAt(source.getLastSyncAt())
                .createdAt(source.getCreatedAt())
                .updatedAt(source.getUpdatedAt())
                .build();
    }

    @Data
    @Builder
    public static class TransformRuleResponse {
        private String field;
        private String sourceField;
        private String type;
        private Map<String, String> mapValues;
    }
}
