package kz.ktj.digitaltwin.gateway.services;

import kz.ktj.digitaltwin.gateway.dto.health.UpsertHealthParamWeightRequest;
import kz.ktj.digitaltwin.gateway.entities.HealthParamWeight;
import kz.ktj.digitaltwin.gateway.repositories.HealthParamWeightRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class HealthParamWeightService {

    private final HealthParamWeightRepository repo;

    public HealthParamWeightService(HealthParamWeightRepository repo) {
        this.repo = repo;
    }

    public List<HealthParamWeight> list() {
        return repo.findAll();
    }

    public HealthParamWeight get(UUID id) {
        return repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("HealthParamWeight not found: " + id));
    }

    @Transactional
    public HealthParamWeight upsert(UpsertHealthParamWeightRequest req) {
        if (req == null) throw new IllegalArgumentException("Request is null");
        if (req.paramName == null || req.paramName.isBlank()) throw new IllegalArgumentException("paramName is required");
        if (req.displayName == null || req.displayName.isBlank()) throw new IllegalArgumentException("displayName is required");
        if (req.weight == null) throw new IllegalArgumentException("weight is required");

        String paramName = req.paramName.trim();

        HealthParamWeight e = repo.findByParamName(paramName).orElseGet(HealthParamWeight::new);

        e.setParamName(paramName);
        e.setDisplayName(req.displayName.trim());
        e.setWeight(req.weight);

        if (req.penaltyMultiplier != null) e.setPenaltyMultiplier(req.penaltyMultiplier);
        if (req.warningThreshold != null) e.setWarningThreshold(req.warningThreshold);
        if (req.criticalThreshold != null) e.setCriticalThreshold(req.criticalThreshold);
        if (req.applicableTo != null) e.setApplicableTo(req.applicableTo);

        validate(e);
        return repo.save(e);
    }

    @Transactional
    public void delete(UUID id) {
        if (!repo.existsById(id)) {
            throw new IllegalArgumentException("HealthParamWeight not found: " + id);
        }
        repo.deleteById(id);
    }

    private void validate(HealthParamWeight e) {
        if (e.getWeight() < 0d) throw new IllegalArgumentException("weight must be >= 0");
        if (e.getPenaltyMultiplier() < 0d) throw new IllegalArgumentException("penaltyMultiplier must be >= 0");

        if (e.getWarningThreshold() < 0d || e.getWarningThreshold() > 1d)
            throw new IllegalArgumentException("warningThreshold must be in [0..1]");

        if (e.getCriticalThreshold() < 0d || e.getCriticalThreshold() > 1d)
            throw new IllegalArgumentException("criticalThreshold must be in [0..1]");
    }
}
