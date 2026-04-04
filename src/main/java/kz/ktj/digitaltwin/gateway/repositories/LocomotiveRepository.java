package kz.ktj.digitaltwin.gateway.repositories;

import kz.ktj.digitaltwin.gateway.entities.Locomotive;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface LocomotiveRepository extends JpaRepository<Locomotive, UUID> {
    Optional<Locomotive> findByCode(String code);
    boolean existsByCode(String code);
}

