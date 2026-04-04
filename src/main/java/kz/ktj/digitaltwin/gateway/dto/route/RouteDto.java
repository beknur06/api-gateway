package kz.ktj.digitaltwin.gateway.dto.route;

import kz.ktj.digitaltwin.gateway.entities.Route;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RouteDto {

    private UUID id;
    private String routeId;
    private String name;
    private double totalKm;
    private List<RouteWaypointDto> waypoints;

    public static RouteDto from(Route r) {
        return new RouteDto(
            r.getId(),
            r.getRouteId(),
            r.getName(),
            r.getTotalKm(),
            r.getWaypoints().stream().map(RouteWaypointDto::from).toList()
        );
    }
}
