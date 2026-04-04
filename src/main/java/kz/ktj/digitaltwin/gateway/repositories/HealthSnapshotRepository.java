package kz.ktj.digitaltwin.gateway.repositories;

import kz.ktj.digitaltwin.gateway.dto.HealthSnapshotDto;
import kz.ktj.digitaltwin.gateway.entities.HealthSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

import io.swagger.v3.oas.annotations.tags.Tag;

@Repository
@Tag(name = "Repositories", description = "DAO слой (не REST). В Swagger UI обычно не отображается.")
public interface HealthSnapshotRepository extends JpaRepository<HealthSnapshot, Long> {

    @Query("""
        SELECT new kz.ktj.digitaltwin.gateway.dto.HealthSnapshotDto(
            CAST(h.calculatedAt AS string), h.score, h.category
        )
        FROM HealthSnapshot h
        WHERE h.locomotiveId = :locomotiveId AND h.calculatedAt BETWEEN :from AND :to
        ORDER BY h.calculatedAt ASC
        """)
    List<HealthSnapshotDto> findHistory(
            @Param("locomotiveId") String locomotiveId,
            @Param("from") Instant from,
            @Param("to") Instant to
    );

    @Query("""
    SELECT new kz.ktj.digitaltwin.gateway.dto.HealthSnapshotDto(
        CAST(h.calculatedAt AS string), h.score, h.category
    )
    FROM HealthSnapshot h
    WHERE h.locomotiveId = :locomotiveId 
      AND h.calculatedAt <= :at
    ORDER BY h.calculatedAt DESC
    LIMIT 1
""")
    HealthSnapshotDto findNearest(@Param("locomotiveId") String locomotiveId, @Param("at") Instant at);
}