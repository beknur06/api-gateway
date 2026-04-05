package kz.ktj.digitaltwin.gateway.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import kz.ktj.digitaltwin.gateway.dto.route.RouteDto;
import kz.ktj.digitaltwin.gateway.dto.route.RouteWaypointDto;
import kz.ktj.digitaltwin.gateway.services.RouteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/routes")
@Tag(name = "Routes", description = "Активные маршруты/состояния локомотивов (из Redis + PostgreSQL)")
public class RoutesController {

    private static final Logger log = LoggerFactory.getLogger(RoutesController.class);

    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;
    private final RouteService routeService;

    public RoutesController(StringRedisTemplate redis, ObjectMapper objectMapper, RouteService routeService) {
        this.redis = redis;
        this.objectMapper = objectMapper;
        this.routeService = routeService;
    }

    @GetMapping
    @Operation(
        summary = "Список активных маршрутов",
        description = "Агрегирует текущее состояние из Redis (last_state:*, health_index:*) и " +
                      "обогащает прогрессом маршрута из PostgreSQL (routeProgressPct, routeWaypoints).",
        responses = @ApiResponse(responseCode = "200", description = "Список активных маршрутов",
            content = @Content(mediaType = "application/json",
                array = @ArraySchema(schema = @Schema(implementation = java.util.Map.class))))
    )
    public ResponseEntity<List<Map<String, Object>>> getActiveRoutes() {
        Set<String> stateKeys = redis.keys("last_state:*");
        if (stateKeys == null || stateKeys.isEmpty()) {
            return ResponseEntity.ok(List.of());
        }

        List<Map<String, Object>> routes = new ArrayList<>();

        for (String key : stateKeys) {
            try {
                String locomotiveId = key.replace("last_state:", "");
                String stateJson  = redis.opsForValue().get(key);
                String healthJson = redis.opsForValue().get("health_index:" + locomotiveId);

                if (stateJson == null) continue;

                JsonNode state  = objectMapper.readTree(stateJson);
                JsonNode health = healthJson != null ? objectMapper.readTree(healthJson) : null;

                Map<String, Object> route = new LinkedHashMap<>();
                route.put("locomotiveId",   locomotiveId);
                route.put("locomotiveType", textOrNull(state, "locomotiveType"));
                route.put("routeId",        textOrNull(state, "routeId"));
                route.put("phase",          textOrNull(state, "phase"));

                JsonNode params = state.get("smoothedParameters");
                if (params == null) params = state.get("rawParameters");
                if (params != null) {
                    route.put("speed",     numOrNull(params, "speed"));
                    route.put("fuelLevel", numOrNull(params, "fuel_level"));
                }

                route.put("gpsLat",   numOrNull(state, "gpsLat"));
                route.put("gpsLon",   numOrNull(state, "gpsLon"));
                route.put("odometer", numOrNull(state, "odometer"));

                if (health != null) {
                    route.put("healthScore",    numOrNull(health, "score"));
                    route.put("healthCategory", textOrNull(health, "category"));
                    route.put("healthTrend",    textOrNull(health, "trend"));
                } else {
                    route.put("healthScore",    null);
                    route.put("healthCategory", "UNKNOWN");
                }

                enrichWithRouteProgress(route);

                routes.add(route);

            } catch (Exception e) {
                log.warn("Failed to parse state for {}: {}", key, e.getMessage());
            }
        }

        routes.sort(Comparator.comparing(r -> (String) r.get("locomotiveId")));
        return ResponseEntity.ok(routes);
    }

    private void enrichWithRouteProgress(Map<String, Object> route) {
        String routeId  = (String) route.get("routeId");
        Double odometer = (Double) route.get("odometer");
        if (routeId == null || odometer == null) return;

        routeService.findByRouteId(routeId).ifPresent(def -> {
            double totalKm     = def.getTotalKm();
            double progressPct = totalKm > 0
                ? Math.min(100.0, (odometer / totalKm) * 100.0)
                : 0.0;

            List<Map<String, Object>> waypointList = def.getWaypoints().stream()
                .map(w -> {
                    Map<String, Object> wm = new LinkedHashMap<>();
                    wm.put("name",        w.getCityName());
                    wm.put("km",          w.getKmFromStart());
                    wm.put("pct",         totalKm > 0
                        ? Math.round((w.getKmFromStart() / totalKm) * 1000.0) / 10.0
                        : 0.0);
                    wm.put("lat",         w.getLat());
                    wm.put("lon",         w.getLon());
                    return wm;
                })
                .collect(Collectors.toList());

            List<RouteWaypointDto> wps = def.getWaypoints();
            String segFrom = wps.get(0).getCityName();
            String segTo   = wps.get(wps.size() - 1).getCityName();
            for (int i = 0; i < wps.size() - 1; i++) {
                if (odometer >= wps.get(i).getKmFromStart() && odometer < wps.get(i + 1).getKmFromStart()) {
                    segFrom = wps.get(i).getCityName();
                    segTo   = wps.get(i + 1).getCityName();
                    break;
                }
            }

            route.put("routeWaypoints",     waypointList);
            route.put("routeTotalKm",        totalKm);
            route.put("routeProgressPct",    Math.round(progressPct * 10.0) / 10.0);
            route.put("currentSegmentFrom",  segFrom);
            route.put("currentSegmentTo",    segTo);
        });
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
