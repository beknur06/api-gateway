package kz.ktj.digitaltwin.gateway.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import kz.ktj.digitaltwin.gateway.dto.locomotives.CreateLocomotiveRequest;
import kz.ktj.digitaltwin.gateway.dto.locomotives.UpdateLocomotiveRequest;
import kz.ktj.digitaltwin.gateway.entities.Locomotive;
import kz.ktj.digitaltwin.gateway.services.LocomotiveService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/locomotives")
@Tag(name = "Locomotives", description = "CRUD по локомотивам")
public class LocomotiveController {

    private final LocomotiveService service;

    public LocomotiveController(LocomotiveService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(
            summary = "Список локомотивов",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Список локомотивов",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = Locomotive.class)))
            }
    )
    public List<Locomotive> list() {
        return service.list();
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Получить локомотив по id",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Локомотив",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = Locomotive.class))),
                    @ApiResponse(responseCode = "400", description = "Некорректный id/ошибка валидации", content = @Content)
            }
    )
    public Locomotive get(
            @Parameter(description = "UUID локомотива", required = true)
            @PathVariable UUID id) {
        return service.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Создать локомотив",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Создано",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = Locomotive.class))),
                    @ApiResponse(responseCode = "400", description = "Ошибка валидации", content = @Content)
            }
    )
    public Locomotive create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Данные для создания",
                    content = @Content(schema = @Schema(implementation = CreateLocomotiveRequest.class))
            )
            @RequestBody CreateLocomotiveRequest req) {
        return service.create(req);
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Обновить локомотив",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Обновлено",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = Locomotive.class))),
                    @ApiResponse(responseCode = "400", description = "Ошибка валидации/не найден", content = @Content)
            }
    )
    public Locomotive update(
            @Parameter(description = "UUID локомотива", required = true)
            @PathVariable UUID id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Поля для обновления (частично)",
                    content = @Content(schema = @Schema(implementation = UpdateLocomotiveRequest.class))
            )
            @RequestBody UpdateLocomotiveRequest req) {
        return service.update(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Удалить локомотив",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Удалено", content = @Content),
                    @ApiResponse(responseCode = "400", description = "Не найден/ошибка валидации", content = @Content)
            }
    )
    public void delete(
            @Parameter(description = "UUID локомотива", required = true)
            @PathVariable UUID id) {
        service.delete(id);
    }
}
