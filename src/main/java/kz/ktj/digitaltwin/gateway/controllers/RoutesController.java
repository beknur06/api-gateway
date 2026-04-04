package kz.ktj.digitaltwin.gateway.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

/**
 * GET /api/v1/routes — список всех активных маршрутов с текущим состоянием.
 *
 * Этот эндпоинт питает первый экран UI (Routes list):
 *   - Маршрут (Astana → Almaty)
 *   - Locomotive ID
 *   - Текущая позиция на маршруте (%)
 *   - Health score + category
 *   - Скорость, статус
 *
 * Данные берутся из Redis (last_state + health_index).
 */
@RestController
@RequestMapping("/api/v1/routes")
public class RoutesController {

    private static final Logger log = LoggerFactory.getLogger(RoutesController.class);

    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;

    public RoutesController(StringRedisTemplate redis, ObjectMapper objectMapper) {
        this.redis = redis;
        this.objectMapper = objectMapper;
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getActiveRoutes() {
        // Scan Redis for all last_state:* keys
        Set<String> stateKeys = redis.keys("last_state:*");
        if (stateKeys == null || stateKeys.isEmpty()) {
            return ResponseEntity.ok(List.of());
        }

        List<Map<String, Object>> routes = new ArrayList<>();

        for (String key : stateKeys) {
            try {
                String locomotiveId = key.replace("last_state:", "");
                String stateJson = redis.opsForValue().get(key);
                String healthJson = redis.opsForValue().get("health_index:" + locomotiveId);

                if (stateJson == null) continue;

                JsonNode state = objectMapper.readTree(stateJson);
                JsonNode health = healthJson != null ? objectMapper.readTree(healthJson) : null;

                Map<String, Object> route = new LinkedHashMap<>();
                route.put("locomotiveId", locomotiveId);
                route.put("locomotiveType", textOrNull(state, "locomotiveType"));
                route.put("routeId", textOrNull(state, "routeId"));
                route.put("phase", textOrNull(state, "phase"));

                // Current telemetry snapshot
                JsonNode params = state.get("smoothedParameters");
                if (params == null) params = state.get("rawParameters");
                if (params != null) {
                    route.put("speed", numOrNull(params, "speed"));
                    route.put("fuelLevel", numOrNull(params, "fuel_level"));
                }

                route.put("gpsLat", numOrNull(state, "gpsLat"));
                route.put("gpsLon", numOrNull(state, "gpsLon"));
                route.put("odometer", numOrNull(state, "odometer"));

                // Health index
                if (health != null) {
                    route.put("healthScore", numOrNull(health, "score"));
                    route.put("healthCategory", textOrNull(health, "category"));
                    route.put("healthTrend", textOrNull(health, "trend"));
                } else {
                    route.put("healthScore", null);
                    route.put("healthCategory", "UNKNOWN");
                }

                routes.add(route);

            } catch (Exception e) {
                log.warn("Failed to parse state for {}: {}", key, e.getMessage());
            }
        }

        // Sort by locomotive ID
        routes.sort(Comparator.comparing(r -> (String) r.get("locomotiveId")));

        return ResponseEntity.ok(routes);
    }

    private String textOrNull(JsonNode node, String field) {
        JsonNode f = node.get(field);
        return f != null && !f.isNull() ? f.asText() : null;
    }

    private Double numOrNull(JsonNode node, String field) {
        JsonNode f = node.get(field);
        return f != null && !f.isNull() ? f.asDouble() : null;
    }
}
