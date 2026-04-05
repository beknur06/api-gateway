package kz.ktj.digitaltwin.gateway.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import kz.ktj.digitaltwin.gateway.dto.threshold.ThresholdResponse;
import kz.ktj.digitaltwin.gateway.dto.threshold.UpsertThresholdRequest;
import kz.ktj.digitaltwin.gateway.services.ThresholdService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/thresholds")
@Tag(name = "Thresholds", description = "Настройка порогов алертов по параметрам")
public class ThresholdsController {

    private final ThresholdService service;

    public ThresholdsController(ThresholdService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(
        summary = "Список порогов",
        description = "Если передан locomotiveId — возвращает только пороги, применимые к этому локомотиву (модель + BOTH).",
        responses = @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(mediaType = "application/json",
                array = @ArraySchema(schema = @Schema(implementation = ThresholdResponse.class))))
    )
    public List<ThresholdResponse> list(
            @Parameter(description = "ID локомотива для фильтрации, напр. KZ8A-0042")
            @RequestParam(required = false) String locomotiveId) {
        return service.list(locomotiveId);
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Получить порог по ID",
        responses = {
            @ApiResponse(responseCode = "200", description = "OK",
                content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ThresholdResponse.class))),
            @ApiResponse(responseCode = "400", description = "Не найдено", content = @Content)
        }
    )
    public ResponseEntity<ThresholdResponse> get(@PathVariable UUID id) {
        try {
            return ResponseEntity.ok(service.get(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping
    @Operation(
        summary = "Создать или обновить порог",
        description = """
            Upsert по (paramName + applicableTo).
            Если передан locomotiveId, applicableTo определяется автоматически из модели локомотива.

            Валидация:
            - criticalHigh > warningHigh (если оба заданы)
            - criticalLow < warningLow (если оба заданы)
            - warningLow < warningHigh (если оба заданы)
            """,
        responses = {
            @ApiResponse(responseCode = "200", description = "Сохранено",
                content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ThresholdResponse.class))),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации", content = @Content)
        }
    )
    public ResponseEntity<ThresholdResponse> upsert(@RequestBody UpsertThresholdRequest req) {
        try {
            return ResponseEntity.ok(service.upsert(req));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
        summary = "Удалить порог",
        responses = {
            @ApiResponse(responseCode = "204", description = "Удалено", content = @Content),
            @ApiResponse(responseCode = "400", description = "Не найдено", content = @Content)
        }
    )
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        try {
            service.delete(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
