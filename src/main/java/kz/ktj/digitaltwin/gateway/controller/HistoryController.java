package kz.ktj.digitaltwin.gateway.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Instant;
import java.util.*;

/**
 * History & export endpoints for the Replay screen (second screenshot).
 *
 * GET /api/v1/history/{locomotiveId}          — telemetry for replay timeline
 * GET /api/v1/history/{locomotiveId}/export   — CSV download for report
 */
@RestController
@RequestMapping("/api/v1/history")
public class HistoryController {

    private static final Logger log = LoggerFactory.getLogger(HistoryController.class);
    private final DataSource clickHouse;

    public HistoryController(DataSource clickHouseDataSource) {
        this.clickHouse = clickHouseDataSource;
    }

    /**
     * GET /api/v1/history/{locomotiveId}?from=...&to=...&resolution=raw|1min
     *
     * Returns telemetry grouped by timestamp for replay:
     * [
     *   { "timestamp": "...", "speed": 82.3, "coolant_temp": 76.1, ... },
     *   ...
     * ]
     */
    @GetMapping("/{locomotiveId}")
    public ResponseEntity<List<Map<String, Object>>> getHistory(
            @PathVariable String locomotiveId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(defaultValue = "raw") String resolution) {

        try {
            List<Map<String, Object>> data;
            if ("1min".equals(resolution)) {
                data = queryAggregated(locomotiveId, from, to);
            } else {
                data = queryRaw(locomotiveId, from, to);
            }
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            log.error("History query failed: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * GET /api/v1/history/{locomotiveId}/export?from=...&to=...&format=csv
     *
     * Downloads CSV file with all parameters for the time window.
     */
    @GetMapping("/{locomotiveId}/export")
    public ResponseEntity<String> export(
            @PathVariable String locomotiveId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {

        try {
            StringBuilder csv = new StringBuilder();
            csv.append("timestamp,param_name,value,phase\n");

            String sql = """
                SELECT event_time, param_name, value, phase
                FROM telemetry_raw
                WHERE locomotive_id = ? AND event_time BETWEEN ? AND ?
                ORDER BY event_time, param_name
                """;

            try (Connection conn = clickHouse.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, locomotiveId);
                ps.setObject(2, java.sql.Timestamp.from(from));
                ps.setObject(3, java.sql.Timestamp.from(to));

                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    csv.append(rs.getTimestamp(1).toInstant())
                       .append(",").append(rs.getString(2))
                       .append(",").append(rs.getDouble(3))
                       .append(",").append(rs.getString(4))
                       .append("\n");
                }
            }

            String filename = locomotiveId + "_" + from.toString().replace(":", "-") + ".csv";

            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv.toString());

        } catch (Exception e) {
            log.error("Export failed: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Raw data query — pivots rows into columnar JSON per timestamp.
     */
    private List<Map<String, Object>> queryRaw(String locomotiveId, Instant from, Instant to)
            throws Exception {

        String sql = """
            SELECT event_time, param_name, value, phase
            FROM telemetry_raw
            WHERE locomotive_id = ? AND event_time BETWEEN ? AND ?
            ORDER BY event_time, param_name
            """;

        Map<Instant, Map<String, Object>> grouped = new LinkedHashMap<>();

        try (Connection conn = clickHouse.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, locomotiveId);
            ps.setObject(2, java.sql.Timestamp.from(from));
            ps.setObject(3, java.sql.Timestamp.from(to));

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Instant ts = rs.getTimestamp(1).toInstant();
                String param = rs.getString(2);
                double value = rs.getDouble(3);
                String phase = rs.getString(4);

                Map<String, Object> row = grouped.computeIfAbsent(ts, k -> {
                    Map<String, Object> r = new LinkedHashMap<>();
                    r.put("timestamp", k.toString());
                    r.put("phase", phase);
                    return r;
                });
                row.put(param, Math.round(value * 100.0) / 100.0);
            }
        }

        return new ArrayList<>(grouped.values());
    }

    /**
     * 1-min aggregated query for longer time windows.
     */
    private List<Map<String, Object>> queryAggregated(String locomotiveId, Instant from, Instant to)
            throws Exception {

        String sql = """
            SELECT event_time, param_name, avg_value, min_value, max_value
            FROM telemetry_1min_agg
            WHERE locomotive_id = ? AND event_time BETWEEN ? AND ?
            ORDER BY event_time, param_name
            """;

        Map<Instant, Map<String, Object>> grouped = new LinkedHashMap<>();

        try (Connection conn = clickHouse.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, locomotiveId);
            ps.setObject(2, java.sql.Timestamp.from(from));
            ps.setObject(3, java.sql.Timestamp.from(to));

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Instant ts = rs.getTimestamp(1).toInstant();
                String param = rs.getString(2);

                Map<String, Object> row = grouped.computeIfAbsent(ts, k -> {
                    Map<String, Object> r = new LinkedHashMap<>();
                    r.put("timestamp", k.toString());
                    return r;
                });
                row.put(param, Math.round(rs.getDouble(3) * 100.0) / 100.0);
                row.put(param + "_min", Math.round(rs.getDouble(4) * 100.0) / 100.0);
                row.put(param + "_max", Math.round(rs.getDouble(5) * 100.0) / 100.0);
            }
        }

        return new ArrayList<>(grouped.values());
    }
}
