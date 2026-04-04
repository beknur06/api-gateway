package kz.ktj.digitaltwin.gateway.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import kz.ktj.digitaltwin.gateway.dto.route.CreateRouteRequest;
import kz.ktj.digitaltwin.gateway.dto.route.RouteDto;
import kz.ktj.digitaltwin.gateway.services.RouteService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/route-definitions")
@Tag(name = "Route Definitions", description = "CRUD маршрутов с точками остановок (из PostgreSQL)")
public class RouteDefinitionsController {

    private final RouteService routeService;

    public RouteDefinitionsController(RouteService routeService) {
        this.routeService = routeService;
    }

    @GetMapping
    @Operation(
        summary = "Список всех маршрутов",
        responses = @ApiResponse(responseCode = "200", description = "Список маршрутов",
            content = @Content(mediaType = "application/json",
                array = @ArraySchema(schema = @Schema(implementation = RouteDto.class))))
    )
    public List<RouteDto> list() {
        return routeService.list();
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Маршрут по UUID",
        responses = {
            @ApiResponse(responseCode = "200", description = "Маршрут",
                content = @Content(schema = @Schema(implementation = RouteDto.class))),
            @ApiResponse(responseCode = "400", description = "Не найден", content = @Content)
        }
    )
    public RouteDto get(
        @Parameter(description = "UUID маршрута") @PathVariable UUID id) {
        return routeService.get(id);
    }

    @GetMapping("/by-route-id/{routeId}")
    @Operation(
        summary = "Маршрут по routeId (строковый идентификатор симулятора)",
        responses = {
            @ApiResponse(responseCode = "200", description = "Маршрут",
                content = @Content(schema = @Schema(implementation = RouteDto.class))),
            @ApiResponse(responseCode = "404", description = "Не найден", content = @Content)
        }
    )
    public RouteDto getByRouteId(
        @Parameter(description = "Например: ASTANA-ALMATY") @PathVariable String routeId) {
        return routeService.findByRouteId(routeId)
            .orElseThrow(() -> new IllegalArgumentException("Route not found: " + routeId));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
        summary = "Создать маршрут с точками",
        responses = {
            @ApiResponse(responseCode = "201", description = "Создан",
                content = @Content(schema = @Schema(implementation = RouteDto.class))),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации", content = @Content)
        }
    )
    public RouteDto create(@RequestBody CreateRouteRequest req) {
        return routeService.create(req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
        summary = "Удалить маршрут",
        responses = {
            @ApiResponse(responseCode = "204", description = "Удалён", content = @Content),
            @ApiResponse(responseCode = "400", description = "Не найден", content = @Content)
        }
    )
    public void delete(
        @Parameter(description = "UUID маршрута") @PathVariable UUID id) {
        routeService.delete(id);
    }
}
