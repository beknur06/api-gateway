package kz.ktj.digitaltwin.gateway.repositories;

import kz.ktj.digitaltwin.gateway.entities.ParameterDefinition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ParameterDefinitionRepository extends JpaRepository<ParameterDefinition, UUID> {
    Optional<ParameterDefinition> findByCode(String code);
    List<ParameterDefinition> findByCodeIn(List<String> codes);
}
