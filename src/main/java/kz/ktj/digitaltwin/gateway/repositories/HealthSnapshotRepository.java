package kz.ktj.digitaltwin.gateway.repositories;

import kz.ktj.digitaltwin.gateway.dto.HealthSnapshotDto;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Repository
public class HealthSnapshotRepository {

    private final DataSource postgres;

    public HealthSnapshotRepository(DataSource postgresDataSource) {
        this.postgres = postgresDataSource;
    }

    public List<HealthSnapshotDto> findHistory(String locomotiveId, Instant from, Instant to) throws Exception {
        String sql = """
            SELECT calculated_at, score, category
            FROM health_snapshots
            WHERE locomotive_id = ? AND calculated_at BETWEEN ? AND ?
            ORDER BY calculated_at
            """;

        List<HealthSnapshotDto> out = new ArrayList<>();

        try (Connection conn = postgres.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, locomotiveId);
            ps.setTimestamp(2, Timestamp.from(from));
            ps.setTimestamp(3, Timestamp.from(to));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Timestamp ts = rs.getTimestamp(1);
                    Double score = (Double) rs.getObject(2);
                    String category = rs.getString(3);

                    out.add(new HealthSnapshotDto(
                            ts != null ? ts.toInstant().toString() : null,
                            score,
                            category
                    ));
                }
            }
        }

        return out;
    }

    public HealthSnapshotDto findNearest(String locomotiveId, Instant at) throws Exception {
        String sql = """
            SELECT snapshot_time, score, category
            FROM health_snapshots
            WHERE locomotive_id = ?
            ORDER BY ABS(EXTRACT(EPOCH FROM (calculated_at - ?::timestamptz))) ASC
            LIMIT 1
            """;

        try (Connection conn = postgres.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, locomotiveId);
            ps.setTimestamp(2, Timestamp.from(at));

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                Timestamp ts = rs.getTimestamp(1);
                Double score = (Double) rs.getObject(2);
                String category = rs.getString(3);

                return new HealthSnapshotDto(
                        ts != null ? ts.toInstant().toString() : null,
                        score,
                        category
                );
            }
        }
    }
}