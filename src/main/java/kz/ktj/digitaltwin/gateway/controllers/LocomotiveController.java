package kz.ktj.digitaltwin.gateway.controllers;

import kz.ktj.digitaltwin.gateway.dto.locomotives.CreateLocomotiveRequest;
import kz.ktj.digitaltwin.gateway.dto.locomotives.UpdateLocomotiveRequest;
import kz.ktj.digitaltwin.gateway.entities.Locomotive;
import kz.ktj.digitaltwin.gateway.services.LocomotiveService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/locomotives")
public class LocomotiveController {

    private final LocomotiveService service;

    public LocomotiveController(LocomotiveService service) {
        this.service = service;
    }

    @GetMapping
    public List<Locomotive> list() {
        return service.list();
    }

    @GetMapping("/{id}")
    public Locomotive get(@PathVariable UUID id) {
        return service.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Locomotive create(@RequestBody CreateLocomotiveRequest req) {
        return service.create(req);
    }

    @PutMapping("/{id}")
    public Locomotive update(@PathVariable UUID id, @RequestBody UpdateLocomotiveRequest req) {
        return service.update(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        service.delete(id);
    }
}
