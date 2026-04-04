package kz.ktj.digitaltwin.gateway.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Health", description = "Запросы health index (история/ближайшее значение) по локомотиву")
public class HealthIndexController {

    private static final Logger log = LoggerFactory.getLogger(HealthIndexController.class);

    private final HealthSnapshotRepository repository;

    public HealthIndexController(HealthSnapshotRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/{locoId}/history")
    @Operation(
            summary = "История индекса здоровья",
            description = "Возвращает список HealthSnapshotDto за интервал [from; to] (включительно), отсортировано по времени.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Список найденных значений",
                            content = @Content(mediaType = "application/json",
                                    array = @ArraySchema(schema = @Schema(implementation = HealthSnapshotDto.class)))),
                    @ApiResponse(responseCode = "500", description = "Ошибка сервера", content = @Content)
            }
    )
    public ResponseEntity<List<HealthSnapshotDto>> history(
            @Parameter(description = "Идентификатор локомотива", example = "L-001", required = true)
            @PathVariable String locoId,
            @Parameter(description = "Начало интервала (ISO-8601)", example = "2026-01-01T00:00:00Z", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @Parameter(description = "Конец интервала (ISO-8601)", example = "2026-01-02T00:00:00Z", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {

        try {
            return ResponseEntity.ok(repository.findHistory(locoId, from, to));
        } catch (Exception e) {
            log.error("Health history query failed: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{locoId}/nearest")
    @Operation(
            summary = "Ближайший индекс здоровья на момент времени",
            description = "Возвращает ближайший доступный HealthSnapshotDto на момент 'at' (обычно последнее значение <= at).",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Найдено значение",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = HealthSnapshotDto.class))),
                    @ApiResponse(responseCode = "404", description = "Нет данных для локомотива/момента", content = @Content),
                    @ApiResponse(responseCode = "500", description = "Ошибка сервера", content = @Content)
            }
    )
    public ResponseEntity<HealthSnapshotDto> nearest(
            @Parameter(description = "Идентификатор локомотива", example = "L-001", required = true)
            @PathVariable String locoId,
            @Parameter(description = "Момент времени (ISO-8601)", example = "2026-01-01T12:00:00Z", required = true)
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