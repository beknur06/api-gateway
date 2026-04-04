package kz.ktj.digitaltwin.gateway.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
    name = "routes",
    uniqueConstraints = @UniqueConstraint(name = "uk_routes_route_id", columnNames = "route_id")
)
@Data
@EqualsAndHashCode(exclude = "waypoints")
@ToString(exclude = "waypoints")
public class Route {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    /** Идентификатор маршрута из симулятора, например "ASTANA-ALMATY" */
    @Column(name = "route_id", nullable = false, length = 50, unique = true)
    private String routeId;

    /** Отображаемое название маршрута */
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    /** Полная длина маршрута в км (симуляционная шкала) */
    @Column(name = "total_km", nullable = false)
    private double totalKm;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "route", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("sortOrder ASC")
    private List<RouteWaypoint> waypoints = new ArrayList<>();
}
