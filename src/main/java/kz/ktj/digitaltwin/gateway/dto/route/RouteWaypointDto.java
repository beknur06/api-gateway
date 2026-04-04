package kz.ktj.digitaltwin.gateway.dto.route;

import kz.ktj.digitaltwin.gateway.entities.RouteWaypoint;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RouteWaypointDto {

    private UUID id;
    private int sortOrder;
    private String cityName;
    private double kmFromStart;
    private double lat;
    private double lon;

    public static RouteWaypointDto from(RouteWaypoint w) {
        return new RouteWaypointDto(
            w.getId(),
            w.getSortOrder(),
            w.getCityName(),
            w.getKmFromStart(),
            w.getLat(),
            w.getLon()
        );
    }
}
