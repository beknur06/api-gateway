package kz.ktj.digitaltwin.gateway.dto.threshold;

import kz.ktj.digitaltwin.gateway.entities.AlertThreshold;
import lombok.Data;

import java.util.UUID;

@Data
public class ThresholdResponse {

    public UUID id;
    public String paramName;
    public String displayName;
    public String unit;
    public Double warningLow;
    public Double warningHigh;
    public Double criticalLow;
    public Double criticalHigh;
    public String warningRecommendation;
    public String criticalRecommendation;
    public String applicableTo;
    public boolean enabled;

    public static ThresholdResponse from(AlertThreshold t, String unit) {
        ThresholdResponse r = new ThresholdResponse();
        r.id = t.getId();
        r.paramName = t.getParamName();
        r.displayName = t.getDisplayName();
        r.unit = unit;
        r.warningLow = t.getWarningLow();
        r.warningHigh = t.getWarningHigh();
        r.criticalLow = t.getCriticalLow();
        r.criticalHigh = t.getCriticalHigh();
        r.warningRecommendation = t.getWarningRecommendation();
        r.criticalRecommendation = t.getCriticalRecommendation();
        r.applicableTo = t.getApplicableTo();
        r.enabled = t.isEnabled();
        return r;
    }
}
