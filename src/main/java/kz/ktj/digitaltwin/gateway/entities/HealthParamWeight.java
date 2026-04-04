package kz.ktj.digitaltwin.gateway.entities;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Entity
@Table(name = "health_param_weights")
@Data
@Schema(description = "Настройка веса/порогов для параметра, используемого при расчёте healthpoints")
public class HealthParamWeight {

    @Schema(description = "Применимость настройки", example = "BOTH")
    public enum ApplicableTo {
        BOTH, DIESEL, ELECTRIC
    }

    @Id
    @GeneratedValue
    @Column(name = "id", columnDefinition = "uuid")
    @Schema(description = "UUID записи", accessMode = Schema.AccessMode.READ_ONLY)
    private UUID id;

    @Column(name = "param_name", nullable = false)
    @Schema(description = "Системное имя параметра", example = "engine_temperature")
    private String paramName;

    @Column(name = "display_name", nullable = false)
    @Schema(description = "Отображаемое имя параметра", example = "Engine temperature")
    private String displayName;

    @Column(name = "weight", nullable = false)
    @Schema(description = "Вес параметра в расчёте", example = "1.0")
    private double weight;

    @Column(name = "penalty_multiplier", nullable = false)
    @Schema(description = "Мультипликатор штрафа", example = "1.5")
    private double penaltyMultiplier = 1.5d;

    @Column(name = "warning_threshold", nullable = false)
    @Schema(description = "Порог предупреждения (0..1)", example = "0.5")
    private double warningThreshold = 0.5d;

    @Column(name = "critical_threshold", nullable = false)
    @Schema(description = "Критический порог (0..1)", example = "0.8")
    private double criticalThreshold = 0.8d;

    @Enumerated(EnumType.STRING)
    @Column(name = "applicable_to", nullable = false)
    @Schema(description = "Применимость", example = "BOTH")
    private ApplicableTo applicableTo = ApplicableTo.BOTH;
}
