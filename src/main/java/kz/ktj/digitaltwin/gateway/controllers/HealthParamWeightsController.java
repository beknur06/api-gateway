package kz.ktj.digitaltwin.gateway.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import kz.ktj.digitaltwin.gateway.dto.health.UpsertHealthParamWeightRequest;
import kz.ktj.digitaltwin.gateway.entities.HealthParamWeight;
import kz.ktj.digitaltwin.gateway.services.HealthParamWeightService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/health/param-weights")
@Tag(name = "Health Param Weights", description = "Настройка весов/порогов параметров для расчёта healthpoints")
public class HealthParamWeightsController {

    private final HealthParamWeightService service;

    public HealthParamWeightsController(HealthParamWeightService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(
            summary = "Получить список настроек параметров",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "OK",
                            content = @Content(
                                    mediaType = "application/json",
                                    array = @ArraySchema(schema = @Schema(implementation = HealthParamWeight.class))
                            )
                    )
            }
    )
    public List<HealthParamWeight> list() {
        return service.list();
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Получить настройку параметра по id",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "OK",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = HealthParamWeight.class)
                            )
                    ),
                    @ApiResponse(responseCode = "400", description = "Некорректный запрос / не найдено", content = @Content)
            }
    )
    public HealthParamWeight get(
            @Parameter(description = "UUID записи", required = true)
            @PathVariable UUID id
    ) {
        return service.get(id);
    }

    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Создать или обновить настройку (upsert по paramName)",
            description = "Если запись с таким paramName существует — обновляется, иначе создаётся новая.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "OK",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = HealthParamWeight.class)
                            )
                    ),
                    @ApiResponse(responseCode = "400", description = "Ошибка валидации", content = @Content)
            }
    )
    public HealthParamWeight upsert(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UpsertHealthParamWeightRequest.class)
                    )
            )
            @RequestBody UpsertHealthParamWeightRequest req
    ) {
        return service.upsert(req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Удалить настройку параметра",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Удалено", content = @Content),
                    @ApiResponse(responseCode = "400", description = "Некорректный запрос / не найдено", content = @Content)
            }
    )
    public void delete(
            @Parameter(description = "UUID записи", required = true)
            @PathVariable UUID id
    ) {
        service.delete(id);
    }
}
