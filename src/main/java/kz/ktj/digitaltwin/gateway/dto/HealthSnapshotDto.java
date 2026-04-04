package kz.ktj.digitaltwin.gateway.dto;

import lombok.Data;

@Data
public class HealthSnapshotDto {
    private String timestamp;
    private Double score;
    private String category;
    private String trend;

    public HealthSnapshotDto(String timestamp, Double score, String category, String trend) {
        this.timestamp = timestamp;
        this.score = score;
        this.category = category;
        this.trend = trend;
    }
}