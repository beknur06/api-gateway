package kz.ktj.digitaltwin.gateway.services;

import io.swagger.v3.oas.annotations.tags.Tag;
import kz.ktj.digitaltwin.gateway.dto.locomotives.CreateLocomotiveRequest;
import kz.ktj.digitaltwin.gateway.dto.locomotives.UpdateLocomotiveRequest;
import kz.ktj.digitaltwin.gateway.entities.Locomotive;
import kz.ktj.digitaltwin.gateway.repositories.LocomotiveRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Tag(name = "Services", description = "Service слой (не REST). В Swagger UI обычно не отображается.")
public class LocomotiveService {

    private final LocomotiveRepository repo;

    public LocomotiveService(LocomotiveRepository repo) {
        this.repo = repo;
    }

    public List<Locomotive> list() {
        return repo.findAll();
    }

    public Locomotive get(UUID id) {
        return repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Locomotive not found: " + id));
    }

    @Transactional
    public Locomotive create(CreateLocomotiveRequest req) {
        if (req == null) throw new IllegalArgumentException("Request is null");
        if (req.code == null || req.code.isBlank()) throw new IllegalArgumentException("code is required");
        if (req.model == null || req.model.isBlank()) throw new IllegalArgumentException("model is required");
        if (req.type == null) throw new IllegalArgumentException("type is required");
        if (repo.existsByCode(req.code)) throw new IllegalArgumentException("code already exists: " + req.code);

        Locomotive e = new Locomotive();
        e.setCode(req.code);
        e.setModel(req.model);
        e.setType(req.type);
        if (req.status != null) e.setStatus(req.status);
        e.setManufacturedAt(req.manufacturedAt);

        return repo.save(e);
    }

    @Transactional
    public Locomotive update(UUID id, UpdateLocomotiveRequest req) {
        if (req == null) throw new IllegalArgumentException("Request is null");

        Locomotive e = get(id);

        if (req.code != null) {
            String newCode = req.code.trim();
            if (newCode.isEmpty()) throw new IllegalArgumentException("code cannot be blank");
            if (!newCode.equals(e.getCode()) && repo.existsByCode(newCode)) {
                throw new IllegalArgumentException("code already exists: " + newCode);
            }
            e.setCode(newCode);
        }

        if (req.model != null) {
            String newModel = req.model.trim();
            if (newModel.isEmpty()) throw new IllegalArgumentException("model cannot be blank");
            e.setModel(newModel);
        }

        if (req.type != null) e.setType(req.type);
        if (req.status != null) e.setStatus(req.status);
        if (req.manufacturedAt != null) e.setManufacturedAt(req.manufacturedAt);

        return repo.save(e);
    }

    @Transactional
    public void delete(UUID id) {
        if (!repo.existsById(id)) throw new IllegalArgumentException("Locomotive not found: " + id);
        repo.deleteById(id);
    }
}
