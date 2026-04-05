package kz.ktj.digitaltwin.gateway.services;

import kz.ktj.digitaltwin.gateway.dto.threshold.ThresholdResponse;
import kz.ktj.digitaltwin.gateway.dto.threshold.UpsertThresholdRequest;
import kz.ktj.digitaltwin.gateway.entities.AlertThreshold;
import kz.ktj.digitaltwin.gateway.entities.Locomotive;
import kz.ktj.digitaltwin.gateway.entities.ParameterDefinition;
import kz.ktj.digitaltwin.gateway.repositories.AlertThresholdRepository;
import kz.ktj.digitaltwin.gateway.repositories.LocomotiveRepository;
import kz.ktj.digitaltwin.gateway.repositories.ParameterDefinitionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ThresholdService {

    private static final Logger log = LoggerFactory.getLogger(ThresholdService.class);

    private final AlertThresholdRepository thresholdRepo;
    private final LocomotiveRepository locomotiveRepo;
    private final ParameterDefinitionRepository paramRepo;

    public ThresholdService(AlertThresholdRepository thresholdRepo,
                            LocomotiveRepository locomotiveRepo,
                            ParameterDefinitionRepository paramRepo) {
        this.thresholdRepo = thresholdRepo;
        this.locomotiveRepo = locomotiveRepo;
        this.paramRepo = paramRepo;
    }

    public List<ThresholdResponse> list(String locomotiveId) {
        List<AlertThreshold> thresholds;

        if (locomotiveId != null && !locomotiveId.isBlank()) {
            String model = resolveModel(locomotiveId);
            thresholds = thresholdRepo.findByApplicableToIn(List.of(model, "BOTH"));
        } else {
            thresholds = thresholdRepo.findAll();
        }

        Map<String, String> unitMap = buildUnitMap(thresholds);

        return thresholds.stream()
            .map(t -> ThresholdResponse.from(t, unitMap.get(t.getParamName())))
            .collect(Collectors.toList());
    }

    public ThresholdResponse get(UUID id) {
        AlertThreshold t = thresholdRepo.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Threshold not found: " + id));
        String unit = paramRepo.findByCode(t.getParamName()).map(ParameterDefinition::getUnit).orElse(null);
        return ThresholdResponse.from(t, unit);
    }

    @Transactional
    public ThresholdResponse upsert(UpsertThresholdRequest req) {
        if (req.paramName == null || req.paramName.isBlank())
            throw new IllegalArgumentException("paramName is required");

        String paramName = req.paramName.trim();

        String applicableTo;
        if (req.locomotiveId != null && !req.locomotiveId.isBlank()) {
            applicableTo = resolveModel(req.locomotiveId.trim());
            log.info("Resolved applicableTo='{}' from locomotiveId='{}'", applicableTo, req.locomotiveId);
        } else if (req.applicableTo != null && !req.applicableTo.isBlank()) {
            applicableTo = req.applicableTo.trim().toUpperCase();
        } else {
            applicableTo = "BOTH";
        }

        AlertThreshold t = thresholdRepo
            .findByParamNameAndApplicableTo(paramName, applicableTo)
            .orElseGet(AlertThreshold::new);

        t.setParamName(paramName);
        t.setApplicableTo(applicableTo);

        if (req.displayName != null)             t.setDisplayName(req.displayName.trim());
        if (req.warningLow != null)              t.setWarningLow(req.warningLow);
        if (req.warningHigh != null)             t.setWarningHigh(req.warningHigh);
        if (req.criticalLow != null)             t.setCriticalLow(req.criticalLow);
        if (req.criticalHigh != null)            t.setCriticalHigh(req.criticalHigh);
        if (req.warningRecommendation != null)   t.setWarningRecommendation(req.warningRecommendation.trim());
        if (req.criticalRecommendation != null)  t.setCriticalRecommendation(req.criticalRecommendation.trim());
        if (req.enabled != null)                 t.setEnabled(req.enabled);

        validate(t);

        AlertThreshold saved = thresholdRepo.save(t);
        String unit = paramRepo.findByCode(paramName).map(ParameterDefinition::getUnit).orElse(null);
        return ThresholdResponse.from(saved, unit);
    }

    @Transactional
    public void delete(UUID id) {
        if (!thresholdRepo.existsById(id))
            throw new IllegalArgumentException("Threshold not found: " + id);
        thresholdRepo.deleteById(id);
    }

    private String resolveModel(String locomotiveId) {
        Locomotive loco = locomotiveRepo.findByCode(locomotiveId)
            .orElseThrow(() -> new IllegalArgumentException("Locomotive not found: " + locomotiveId));
        return loco.getModel();
    }

    private Map<String, String> buildUnitMap(List<AlertThreshold> thresholds) {
        List<String> paramNames = thresholds.stream().map(AlertThreshold::getParamName).distinct().toList();
        return paramRepo.findByCodeIn(paramNames).stream()
            .collect(Collectors.toMap(ParameterDefinition::getCode, ParameterDefinition::getUnit,
                (a, b) -> a));
    }

    private void validate(AlertThreshold t) {
        if (t.getWarningHigh() != null && t.getCriticalHigh() != null
                && t.getCriticalHigh() <= t.getWarningHigh())
            throw new IllegalArgumentException(
                "criticalHigh (" + t.getCriticalHigh() + ") must be > warningHigh (" + t.getWarningHigh() + ")");

        if (t.getWarningLow() != null && t.getCriticalLow() != null
                && t.getCriticalLow() >= t.getWarningLow())
            throw new IllegalArgumentException(
                "criticalLow (" + t.getCriticalLow() + ") must be < warningLow (" + t.getWarningLow() + ")");

        if (t.getWarningLow() != null && t.getWarningHigh() != null
                && t.getWarningLow() >= t.getWarningHigh())
            throw new IllegalArgumentException(
                "warningLow (" + t.getWarningLow() + ") must be < warningHigh (" + t.getWarningHigh() + ")");
    }
}
