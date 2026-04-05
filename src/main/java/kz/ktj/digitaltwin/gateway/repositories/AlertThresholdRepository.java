package kz.ktj.digitaltwin.gateway.repositories;

import kz.ktj.digitaltwin.gateway.entities.AlertThreshold;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AlertThresholdRepository extends JpaRepository<AlertThreshold, UUID> {
    List<AlertThreshold> findByApplicableToIn(List<String> applicableTo);
    Optional<AlertThreshold> findByParamNameAndApplicableTo(String paramName, String applicableTo);
}
