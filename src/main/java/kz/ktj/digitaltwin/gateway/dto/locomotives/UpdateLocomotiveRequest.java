package kz.ktj.digitaltwin.gateway.dto.locomotives;

import kz.ktj.digitaltwin.gateway.entities.Locomotive;

import java.time.LocalDate;

public class UpdateLocomotiveRequest {
    public String code;
    public String model;
    public Locomotive.Type type;
    public Locomotive.Status status;
    public LocalDate manufacturedAt;
}

