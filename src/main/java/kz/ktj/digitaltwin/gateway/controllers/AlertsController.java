package kz.ktj.digitaltwin.gateway.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kz.ktj.digitaltwin.gateway.entities.Alert;
import kz.ktj.digitaltwin.gateway.repositories.AlertRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/alerts")
@Tag(name = "Alerts", description = "Алерты локомотивов")
public class AlertsController {

    private final AlertRepository alertRepository;

    public AlertsController(AlertRepository alertRepository) {
        this.alertRepository = alertRepository;
    }

    @GetMapping("/{locomotiveId}")
    @Operation(summary = "50 последних алертов локомотива")
    public List<Alert> getAlerts(@PathVariable String locomotiveId) {
        return alertRepository.findTop50ByLocomotiveIdOrderByTriggeredAtDesc(locomotiveId);
    }

    @GetMapping("/{locomotiveId}/active")
    @Operation(summary = "Активные алерты локомотива")
    public List<Alert> getActiveAlerts(@PathVariable String locomotiveId) {
        return alertRepository.findByLocomotiveIdAndStatusOrderByTriggeredAtDesc(
            locomotiveId, Alert.Status.ACTIVE);
    }

    @GetMapping("/{locomotiveId}/history")
    @Operation(summary = "История алертов за диапазон дат")
    public List<Alert> getHistory(
            @PathVariable String locomotiveId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {
        return alertRepository.findByLocomotiveIdAndTriggeredAtBetweenOrderByTriggeredAtDesc(
            locomotiveId, from, to);
    }

    @PostMapping("/{id}/acknowledge")
    @Operation(summary = "Подтвердить алерт")
    public ResponseEntity<Alert> acknowledge(@PathVariable UUID id) {
        return alertRepository.findById(id)
            .map(alert -> {
                alert.setStatus(Alert.Status.ACKNOWLEDGED);
                alert.setAcknowledgedAt(Instant.now());
                return ResponseEntity.ok(alertRepository.save(alert));
            })
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/resolve")
    @Operation(summary = "Закрыть алерт вручную")
    public ResponseEntity<Alert> resolve(@PathVariable UUID id) {
        return alertRepository.findById(id)
            .map(alert -> {
                alert.setStatus(Alert.Status.RESOLVED);
                alert.setResolvedAt(Instant.now());
                return ResponseEntity.ok(alertRepository.save(alert));
            })
            .orElse(ResponseEntity.notFound().build());
    }
}
