package kz.ktj.digitaltwin.gateway.dto;

import kz.ktj.digitaltwin.gateway.entities.HealthSnapshot;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class HealthSnapshotDto {
    private String timestamp;
    private Double score;
    private String category;

    public HealthSnapshotDto(String timestamp, Double score, HealthSnapshot.Category category) {
        this.timestamp = timestamp;
        this.score = score;
        this.category = category != null ? category.name() : null;
    }
}