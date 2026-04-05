package kz.ktj.digitaltwin.gateway.entities;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "alerts", indexes = {
    @Index(name = "idx_alert_loco_status", columnList = "locomotiveId, status"),
    @Index(name = "idx_alert_triggered",   columnList = "triggeredAt DESC")
})
@Data
public class Alert {

    public enum Severity { INFO, WARNING, CRITICAL }
    public enum Status   { ACTIVE, ACKNOWLEDGED, RESOLVED }

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @Column(nullable = false)
    private String locomotiveId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Severity severity;

    @Column(nullable = false)
    private String paramName;

    private String displayName;

    @Column(nullable = false)
    private double paramValue;

    @Column(nullable = false)
    private double thresholdValue;

    @Column(nullable = false, length = 500)
    private String message;

    @Column(length = 500)
    private String recommendation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.ACTIVE;

    @Column(nullable = false)
    private Instant triggeredAt;

    private Instant acknowledgedAt;
    private Instant resolvedAt;
}
