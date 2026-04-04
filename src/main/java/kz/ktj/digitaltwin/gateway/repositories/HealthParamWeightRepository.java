package kz.ktj.digitaltwin.gateway.repositories;

import kz.ktj.digitaltwin.gateway.entities.HealthParamWeight;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface HealthParamWeightRepository extends JpaRepository<HealthParamWeight, UUID> {
    Optional<HealthParamWeight> findByParamName(String paramName);
    boolean existsByParamName(String paramName);
}
