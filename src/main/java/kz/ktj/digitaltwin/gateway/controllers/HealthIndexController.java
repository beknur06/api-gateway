package kz.ktj.digitaltwin.gateway.controllers;

import kz.ktj.digitaltwin.gateway.dto.HealthSnapshotDto;
import kz.ktj.digitaltwin.gateway.repositories.HealthSnapshotRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/v1/health")
public class HealthIndexController {

    private static final Logger log = LoggerFactory.getLogger(HealthIndexController.class);

    private final HealthSnapshotRepository repository;

    public HealthIndexController(HealthSnapshotRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/{locoId}/history")
    public ResponseEntity<List<HealthSnapshotDto>> history(
            @PathVariable String locoId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {

        try {
            return ResponseEntity.ok(repository.findHistory(locoId, from, to));
        } catch (Exception e) {
            log.error("Health history query failed: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{locoId}/nearest")
    public ResponseEntity<HealthSnapshotDto> nearest(
            @PathVariable String locoId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant at) {

        try {
            HealthSnapshotDto dto = repository.findNearest(locoId, at);
            if (dto == null) return ResponseEntity.notFound().build();
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            log.error("Health nearest query failed: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}