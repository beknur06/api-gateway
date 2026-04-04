package kz.ktj.digitaltwin.gateway.dto.route;

import java.util.List;

public class CreateRouteRequest {

    /** Идентификатор из симулятора, например "ASTANA-ALMATY" */
    public String routeId;

    /** Отображаемое название */
    public String name;

    /** Полная длина маршрута в км */
    public double totalKm;

    /** Список точек маршрута, упорядоченных по sortOrder */
    public List<WaypointRequest> waypoints;

    public static class WaypointRequest {
        public int sortOrder;
        public String cityName;
        public double kmFromStart;
        public double lat;
        public double lon;
    }
}
