package smoma.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Runs before {@code DataLoader}. Hibernate's {@code ddl-auto=update} adds missing columns
 * and tables but never widens or retypes an existing column, so an enum-string column created
 * by an earlier schema (e.g. a MySQL {@code ENUM(...)} or a short {@code VARCHAR}) causes
 * "Data truncated for column 'statut'" on insert. This widens every such column to VARCHAR(64).
 */
@Component
@Order(1)
public class SchemaFixup implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(SchemaFixup.class);

    private static final String[][] ENUM_COLUMNS = {
        {"mandats_de_mission", "statut"},
        {"mandats_de_mission", "type_mission"},
        {"ordres_de_mission",  "statut"},
        {"ordres_de_mission",  "type_mission"},
        {"avances_frais",      "statut"},
        {"avances_frais",      "mode_paiement"},
        {"rapports_mission",   "statut"},
        {"rapports_mission",   "categorie"},
        {"personnel",          "statut"},
        {"personnel",          "grade"},
        {"etapes_mission",     "type_etape"},
        {"baremes_indemnites", "type_mission"},
        {"users",              "role"},
        {"user_roles",         "role"}
    };

    private final JdbcTemplate jdbc;

    public SchemaFixup(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void run(String... args) {
        int fixed = 0;
        for (String[] tc : ENUM_COLUMNS) {
            String table = tc[0], col = tc[1];
            try {
                var rows = jdbc.queryForList(
                        "SELECT DATA_TYPE, CHARACTER_MAXIMUM_LENGTH, IS_NULLABLE " +
                        "FROM information_schema.COLUMNS " +
                        "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND COLUMN_NAME = ?",
                        table, col);
                if (rows.isEmpty()) continue; // table/column not present in this schema

                var row = rows.get(0);
                String type = String.valueOf(row.get("DATA_TYPE")).toLowerCase();
                Object lenObj = row.get("CHARACTER_MAXIMUM_LENGTH");
                long len = lenObj == null ? 0L : ((Number) lenObj).longValue();
                boolean needsWiden = "enum".equals(type) || "char".equals(type) || len < 64;
                if (!needsWiden) continue;

                boolean nullable = !"NO".equalsIgnoreCase(String.valueOf(row.get("IS_NULLABLE")));
                jdbc.execute("ALTER TABLE `" + table + "` MODIFY COLUMN `" + col + "` VARCHAR(64) "
                        + (nullable ? "NULL" : "NOT NULL"));
                log.info("SchemaFixup: {}.{} ({}{}) -> VARCHAR(64)", table, col, type,
                        len > 0 ? "(" + len + ")" : "");
                fixed++;
            } catch (Exception e) {
                log.warn("SchemaFixup: could not adjust {}.{}: {}", table, col, e.getMessage());
            }
        }
        if (fixed > 0) {
            log.info("SchemaFixup: {} enum column(s) widened to VARCHAR(64).", fixed);
        }
    }
}
