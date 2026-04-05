package kz.ktj.digitaltwin.gateway.entities;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Table(name = "alert_thresholds")
@Data
public class AlertThreshold {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @Column(name = "param_name", nullable = false)
    private String paramName;

    @Column(name = "display_name")
    private String displayName;

    @Column(name = "warning_high")
    private Double warningHigh;

    @Column(name = "warning_low")
    private Double warningLow;

    @Column(name = "critical_high")
    private Double criticalHigh;

    @Column(name = "critical_low")
    private Double criticalLow;

    @Column(name = "warning_recommendation", length = 500)
    private String warningRecommendation;

    @Column(name = "critical_recommendation", length = 500)
    private String criticalRecommendation;

    @Column(name = "applicable_to", nullable = false)
    private String applicableTo = "BOTH";

    @Column(name = "enabled", nullable = false)
    private boolean enabled = true;
}
