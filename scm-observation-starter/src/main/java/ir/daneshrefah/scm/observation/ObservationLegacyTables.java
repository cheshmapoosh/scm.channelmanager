package ir.daneshrefah.scm.observation;

import java.util.Set;

public final class ObservationLegacyTables {
    private static final Set<String> ALLOWED_LEGACY_TABLES = Set.of(
            "ib.transaction",
            "rb.message_log",
            "cm.transaction_log",
            "cm.user_action_log"
    );

    private ObservationLegacyTables() {
    }

    public static String validate(String legacyTable) {
        if (legacyTable == null || legacyTable.isBlank()) {
            throw new IllegalArgumentException("scm.target.legacy.table is required when legacy projection is enabled");
        }
        String normalized = legacyTable.trim();
        if (!ALLOWED_LEGACY_TABLES.contains(normalized)) {
            throw new IllegalArgumentException("Unsupported legacy projection table: " + legacyTable);
        }
        return normalized;
    }
}
