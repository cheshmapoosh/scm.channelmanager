package ir.daneshrefah.scm.cache.repository;

import ir.daneshrefah.scm.cache.domain.config.InstanceConfigEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

@Repository
public class JdbcInstanceCacheConfigRepository
        implements InstanceCacheConfigRepository {

    private static final String TABLE_NAME =
            "TBL_CHE_INSTANCE_CONFIG";

    private static final Pattern DATABASE_IDENTIFIER =
            Pattern.compile("[A-Za-z][A-Za-z0-9_]*");

    private static final String SELECT_ALL = """
            SELECT
                INSTANCE_ID,
                INSTANCE_TYPE,
                INSTANCE_NAME,
                STATISTICS_ENABLED,

                TTL_SECONDS,
                BACKUP_COUNT,
                MAX_IDLE_SECONDS,
                EVIC_SIZE,
                EVIC_MAX_SIZE_POLICY,
                ASYNC_BACKUP_COUNT,

                MAX_SIZE,
                MERGE_POLICY_BATCH_SIZE,

                BINARY_ENABLED,
                SPLIT_BRAIN_PROTECTION_NAME,
                VALUE_COLLECTION_TYPE,

                ASYNC_FILL_UP_ENABLED,
                IN_MEMORY_FORMAT,

                PRIORITY_COMP_CLASS_NAME,
                EMPTY_QUEUE_TTL,

                TOPIC_GLOBAL_ORD_ENABLED,
                TOPIC_MULTI_THRD_ENABLED
            FROM %s
            ORDER BY INSTANCE_TYPE, INSTANCE_NAME
            """;

    private final JdbcTemplate jdbcTemplate;
    private final InstanceConfigRowMapper rowMapper;
    private final String selectAllSql;

    public JdbcInstanceCacheConfigRepository(
            JdbcTemplate jdbcTemplate,
            InstanceConfigRowMapper rowMapper,
            @Value("""
                    ${scm.cache.persistence.schema:\
                    ${spring.jpa.properties.hibernate.default_schema:}}
                    """)
            String schema
    ) {
        this.jdbcTemplate = Objects.requireNonNull(
                jdbcTemplate,
                "jdbcTemplate must not be null"
        );
        this.rowMapper = Objects.requireNonNull(
                rowMapper,
                "rowMapper must not be null"
        );
        this.selectAllSql = SELECT_ALL.formatted(
                qualifiedTableName(schema)
        );
    }

    @Override
    public List<InstanceConfigEntity> findAll() {
        List<InstanceConfigEntity> definitions =
                jdbcTemplate.query(selectAllSql, rowMapper);

        return List.copyOf(definitions);
    }

    private static String qualifiedTableName(String schema) {
        if (schema == null || schema.isBlank()) {
            return TABLE_NAME;
        }

        String normalizedSchema =
                schema.trim().toUpperCase(Locale.ROOT);

        if (!DATABASE_IDENTIFIER.matcher(normalizedSchema).matches()) {
            throw new IllegalArgumentException(
                    "Invalid cache database schema: " + schema
            );
        }

        return normalizedSchema + "." + TABLE_NAME;
    }
}