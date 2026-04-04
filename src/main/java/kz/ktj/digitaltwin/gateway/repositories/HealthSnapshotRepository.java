package kz.ktj.digitaltwin.gateway.repositories;

import kz.ktj.digitaltwin.gateway.dto.HealthSnapshotDto;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
            SELECT snapshot_time, score, category, trend
            FROM health_snapshots
            WHERE locomotive_id = ? AND snapshot_time BETWEEN ? AND ?
            ORDER BY snapshot_time
            """;

        List<HealthSnapshotDto> out = new ArrayList<>();

        try (Connection conn = postgres.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, locomotiveId);
            ps.setObject(2, from);
            ps.setObject(3, to);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Instant ts = rs.getObject(1, Instant.class);
                    Double score = (Double) rs.getObject(2);
                    String category = rs.getString(3);
                    String trend = rs.getString(4);

                    out.add(new HealthSnapshotDto(
                            ts != null ? ts.toString() : null,
                            score,
                            category,
                            trend
                    ));
                }
            }
        }

        return out;
    }

    public HealthSnapshotDto findNearest(String locomotiveId, Instant at) throws Exception {
        String sql = """
            SELECT snapshot_time, score, category, trend
            FROM health_snapshots
            WHERE locomotive_id = ?
            ORDER BY ABS(EXTRACT(EPOCH FROM (snapshot_time - ?::timestamptz))) ASC
            LIMIT 1
            """;

        try (Connection conn = postgres.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, locomotiveId);
            ps.setObject(2, at);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                Instant ts = rs.getObject(1, Instant.class);
                Double score = (Double) rs.getObject(2);
                String category = rs.getString(3);
                String trend = rs.getString(4);

                return new HealthSnapshotDto(
                        ts != null ? ts.toString() : null,
                        score,
                        category,
                        trend
                );
            }
        }
    }
}