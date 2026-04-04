package kz.ktj.digitaltwin.gateway.dto.health;

import io.swagger.v3.oas.annotations.media.Schema;
import kz.ktj.digitaltwin.gateway.entities.HealthParamWeight;

@Schema(description = "Запрос на создание/обновление настройки веса/порогов для параметра healthpoints (upsert по paramName)")
public class UpsertHealthParamWeightRequest {

    @Schema(description = "Системное имя параметра (уникально в рамках настроек)", example = "engine_temperature", requiredMode = Schema.RequiredMode.REQUIRED)
    public String paramName;

    @Schema(description = "Отображаемое имя параметра", example = "Engine temperature", requiredMode = Schema.RequiredMode.REQUIRED)
    public String displayName;

    @Schema(description = "Вес параметра в расчёте healthpoints", example = "1.0", requiredMode = Schema.RequiredMode.REQUIRED)
    public Double weight;

    @Schema(description = "Мультипликатор штрафа при превышении порогов", example = "1.5")
    public Double penaltyMultiplier;

    @Schema(description = "Порог предупреждения (0..1)", example = "0.5")
    public Double warningThreshold;

    @Schema(description = "Критический порог (0..1)", example = "0.8")
    public Double criticalThreshold;

    @Schema(description = "Применимость (какой тип локомотива/агрегата)", example = "BOTH")
    public HealthParamWeight.ApplicableTo applicableTo;
}
