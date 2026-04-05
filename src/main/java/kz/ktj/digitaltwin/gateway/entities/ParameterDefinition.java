package kz.ktj.digitaltwin.gateway.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Entity
@Table(name = "parameter_definitions")
@Data
public class ParameterDefinition {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "code", nullable = false, unique = true)
    private String code;

    @Column(name = "name")
    private String name;

    @Column(name = "unit")
    private String unit;

    @Column(name = "min_val")
    private Double minVal;

    @Column(name = "max_val")
    private Double maxVal;
}
