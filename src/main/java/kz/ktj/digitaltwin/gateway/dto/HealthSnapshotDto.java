package kz.ktj.digitaltwin.gateway.dto;

import lombok.Data;

@Data
public class HealthSnapshotDto {
    private String timestamp;
    private Double score;
    private String category;

    public HealthSnapshotDto(String timestamp, Double score, String category) {
        this.timestamp = timestamp;
        this.score = score;
        this.category = category;
    }
}