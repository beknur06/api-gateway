package kz.ktj.digitaltwin.gateway.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kz.ktj.digitaltwin.gateway.repositories.AlertRepository;
import kz.ktj.digitaltwin.gateway.repositories.HealthSnapshotRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/admin")
@Tag(name = "Admin", description = "Очистка данных ClickHouse, Redis и PostgreSQL")
public class AdminController {

    private static final Logger log = LoggerFactory.getLogger(AdminController.class);

    private final DataSource clickHouse;
    private final AlertRepository alertRepository;
    private final HealthSnapshotRepository healthSnapshotRepository;
    private final StringRedisTemplate redis;

    public AdminController(@Qualifier("clickHouseDataSource") DataSource clickHouseDataSource,
                           AlertRepository alertRepository,
                           HealthSnapshotRepository healthSnapshotRepository,
                           StringRedisTemplate redis) {
        this.clickHouse = clickHouseDataSource;
        this.alertRepository = alertRepository;
        this.healthSnapshotRepository = healthSnapshotRepository;
        this.redis = redis;
    }

    @DeleteMapping("/clickhouse")
    @Operation(summary = "Очистить все таблицы телеметрии в ClickHouse")
    public ResponseEntity<String> clearClickHouse() {
        try (Connection conn = clickHouse.getConnection();
             Statement st = conn.createStatement()) {
            st.execute("TRUNCATE TABLE telemetry_raw");
            st.execute("TRUNCATE TABLE telemetry_1min_agg");
            log.info("ClickHouse tables truncated");
            return ResponseEntity.ok("ClickHouse cleared: telemetry_raw, telemetry_1min_agg");
        } catch (Exception e) {
            log.error("Failed to clear ClickHouse: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Failed: " + e.getMessage());
        }
    }

    @DeleteMapping("/redis")
    @Operation(summary = "Очистить кэш телеметрии и health index в Redis")
    public ResponseEntity<String> clearRedis() {
        try {
            int total = deleteRedisKeys();
            log.info("Redis cleared: {} keys deleted", total);
            return ResponseEntity.ok("Redis cleared: " + total + " keys deleted");
        } catch (Exception e) {
            log.error("Failed to clear Redis: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Failed: " + e.getMessage());
        }
    }

    @DeleteMapping("/postgres")
    @Operation(summary = "Очистить таблицы alerts и health_snapshots в PostgreSQL")
    public ResponseEntity<String> clearPostgres() {
        try {
            alertRepository.deleteAll();
            healthSnapshotRepository.deleteAll();
            log.info("PostgreSQL tables cleared");
            return ResponseEntity.ok("PostgreSQL cleared: alerts, health_snapshots");
        } catch (Exception e) {
            log.error("Failed to clear PostgreSQL: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Failed: " + e.getMessage());
        }
    }

    @DeleteMapping("/all")
    @Operation(summary = "Очистить ClickHouse, Redis и PostgreSQL одним запросом")
    public ResponseEntity<String> clearAll() {
        StringBuilder result = new StringBuilder();

        try (Connection conn = clickHouse.getConnection();
             Statement st = conn.createStatement()) {
            st.execute("TRUNCATE TABLE telemetry_raw");
            st.execute("TRUNCATE TABLE telemetry_1min_agg");
            result.append("ClickHouse: cleared. ");
        } catch (Exception e) {
            log.error("Failed to clear ClickHouse: {}", e.getMessage());
            result.append("ClickHouse: FAILED (").append(e.getMessage()).append("). ");
        }

        try {
            int total = deleteRedisKeys();
            result.append("Redis: ").append(total).append(" keys deleted. ");
        } catch (Exception e) {
            log.error("Failed to clear Redis: {}", e.getMessage());
            result.append("Redis: FAILED (").append(e.getMessage()).append("). ");
        }

        try {
            alertRepository.deleteAll();
            healthSnapshotRepository.deleteAll();
            result.append("PostgreSQL: cleared.");
        } catch (Exception e) {
            log.error("Failed to clear PostgreSQL: {}", e.getMessage());
            result.append("PostgreSQL: FAILED (").append(e.getMessage()).append(").");
        }

        log.info("Admin clear all: {}", result);
        return ResponseEntity.ok(result.toString());
    }

    private int deleteRedisKeys() {
        Set<String> keys = redis.keys("last_state:*");
        if (keys != null && !keys.isEmpty()) redis.delete(keys);

        Set<String> healthKeys = redis.keys("health_index:*");
        if (healthKeys != null && !healthKeys.isEmpty()) redis.delete(healthKeys);

        return (keys == null ? 0 : keys.size()) + (healthKeys == null ? 0 : healthKeys.size());
    }
}
