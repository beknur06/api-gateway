package kz.ktj.digitaltwin.gateway.repositories;

import kz.ktj.digitaltwin.gateway.entities.Route;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RouteRepository extends JpaRepository<Route, UUID> {
    Optional<Route> findByRouteId(String routeId);
    boolean existsByRouteId(String routeId);
}
