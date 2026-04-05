package kz.ktj.digitaltwin.gateway.repositories;

import kz.ktj.digitaltwin.gateway.entities.Alert;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface AlertRepository extends JpaRepository<Alert, UUID> {

    List<Alert> findTop50ByLocomotiveIdOrderByTriggeredAtDesc(String locomotiveId);

    List<Alert> findByLocomotiveIdAndStatusOrderByTriggeredAtDesc(String locomotiveId, Alert.Status status);

    List<Alert> findByLocomotiveIdAndTriggeredAtBetweenOrderByTriggeredAtDesc(
        String locomotiveId, Instant from, Instant to);
}
