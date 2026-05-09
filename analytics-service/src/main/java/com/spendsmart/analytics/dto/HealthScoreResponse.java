package com.spendsmart.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthScoreResponse {
    private double score;
    private String status; // "EXCELLENT", "GOOD", "AVERAGE", "POOR"
    private String insight;
}
