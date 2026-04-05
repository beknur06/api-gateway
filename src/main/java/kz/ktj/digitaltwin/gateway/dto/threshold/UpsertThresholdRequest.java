package kz.ktj.digitaltwin.gateway.dto.threshold;

public class UpsertThresholdRequest {

    /** Optional — if provided, resolves applicableTo from the locomotive's model. */
    public String locomotiveId;

    /** System parameter name, e.g. "coolant_temp". Required. */
    public String paramName;

    public String displayName;

    /** BOTH | TE33A | KZ8A. Ignored if locomotiveId is provided. */
    public String applicableTo;

    public Double warningLow;
    public Double warningHigh;
    public Double criticalLow;
    public Double criticalHigh;

    public String warningRecommendation;
    public String criticalRecommendation;

    public Boolean enabled;

    /** Audit metadata — logged but not persisted (no column). */
    public String updatedBy;
}
