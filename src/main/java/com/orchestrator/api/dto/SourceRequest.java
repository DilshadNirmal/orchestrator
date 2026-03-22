package com.orchestrator.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class SourceRequest {
    
    @NotBlank(message="Name is required")
    private String name;

    @NotBlank(message = "Type is required")
    private String type;
    
    private Map<String, Object> config;
    
    private List<TransformRuleRequest> transformRules;
    
    private Boolean enabled = false;

    @Data
    public static class TransformRuleRequest {
        private String field;
        private String sourceField;
        private String type;
        private Map<String, String> mapValues;
    }
}
