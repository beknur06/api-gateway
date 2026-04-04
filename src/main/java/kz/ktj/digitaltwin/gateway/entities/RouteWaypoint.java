package kz.ktj.digitaltwin.gateway.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Table(
    name = "route_waypoints",
    uniqueConstraints = @UniqueConstraint(name = "uk_route_waypoints_route_order", columnNames = {"route_id", "sort_order"})
)
@Data
@EqualsAndHashCode(exclude = "route")
@ToString(exclude = "route")
public class RouteWaypoint {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id", nullable = false)
    private Route route;

    /** Порядок точки на маршруте (0-based или 1-based, главное уникально) */
    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    /** Название города/станции */
    @Column(name = "city_name", nullable = false, length = 100)
    private String cityName;

    /** Километраж от начала маршрута */
    @Column(name = "km_from_start", nullable = false)
    private double kmFromStart;

    /** GPS координаты станции */
    @Column(name = "lat", nullable = false)
    private double lat;

    @Column(name = "lon", nullable = false)
    private double lon;
}
