package kz.ktj.digitaltwin.gateway.services;

import kz.ktj.digitaltwin.gateway.dto.route.CreateRouteRequest;
import kz.ktj.digitaltwin.gateway.dto.route.RouteDto;
import kz.ktj.digitaltwin.gateway.entities.Route;
import kz.ktj.digitaltwin.gateway.entities.RouteWaypoint;
import kz.ktj.digitaltwin.gateway.repositories.RouteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class RouteService {

    private final RouteRepository repo;

    public RouteService(RouteRepository repo) {
        this.repo = repo;
    }

    public List<RouteDto> list() {
        return repo.findAll().stream().map(RouteDto::from).toList();
    }

    public Optional<RouteDto> findByRouteId(String routeId) {
        return repo.findByRouteId(routeId).map(RouteDto::from);
    }

    public RouteDto get(UUID id) {
        Route r = repo.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Route not found: " + id));
        return RouteDto.from(r);
    }

    @Transactional
    public RouteDto create(CreateRouteRequest req) {
        if (req.routeId == null || req.routeId.isBlank())
            throw new IllegalArgumentException("routeId is required");
        if (req.name == null || req.name.isBlank())
            throw new IllegalArgumentException("name is required");
        if (req.totalKm <= 0)
            throw new IllegalArgumentException("totalKm must be positive");
        if (repo.existsByRouteId(req.routeId))
            throw new IllegalArgumentException("routeId already exists: " + req.routeId);

        Route route = new Route();
        route.setRouteId(req.routeId.trim().toUpperCase());
        route.setName(req.name.trim());
        route.setTotalKm(req.totalKm);

        if (req.waypoints != null) {
            for (CreateRouteRequest.WaypointRequest wr : req.waypoints) {
                if (wr.cityName == null || wr.cityName.isBlank())
                    throw new IllegalArgumentException("cityName is required for each waypoint");

                RouteWaypoint wp = new RouteWaypoint();
                wp.setRoute(route);
                wp.setSortOrder(wr.sortOrder);
                wp.setCityName(wr.cityName.trim());
                wp.setKmFromStart(wr.kmFromStart);
                wp.setLat(wr.lat);
                wp.setLon(wr.lon);
                route.getWaypoints().add(wp);
            }
        }

        return RouteDto.from(repo.save(route));
    }

    @Transactional
    public void delete(UUID id) {
        if (!repo.existsById(id))
            throw new IllegalArgumentException("Route not found: " + id);
        repo.deleteById(id);
    }
}
