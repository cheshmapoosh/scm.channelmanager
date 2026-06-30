package ir.daneshrefah.scm.uaa.repository.authentication.nib;

import ir.daneshrefah.scm.common.constant.TerminalType;
import ir.daneshrefah.scm.uaa.exception.activation.nib.NibActivationAlreadyExistsException;
import ir.daneshrefah.scm.uaa.exception.activation.nib.NibActivationDataAccessException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

@Repository
@ConditionalOnProperty(
        prefix = "scm.uaa.activation.nib",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class NibActivationJdbcRepository {
    private static final String USER_CHANNEL_AUTHENTICATION_TABLE = "REF.USER_CHANNEL_AUTHENTICATION";
    private static final String CHANNEL_TABLE = "REF.CHANNEL";

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final JdbcTemplate jdbcTemplate;

    public NibActivationJdbcRepository(
            @Qualifier("mainNamedParameterJdbcTemplate") NamedParameterJdbcTemplate namedParameterJdbcTemplate,
            @Qualifier("mainJdbcTemplate") JdbcTemplate jdbcTemplate
    ) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<Integer> findParentNibChannelId() {
        String sql = "SELECT CHANNEL_ID FROM " + CHANNEL_TABLE
                + " WHERE CODE = :code AND PARENT_ID IS NULL";
        return execute("find parent NIB channel", () -> namedParameterJdbcTemplate.query(
                sql,
                Map.of("code", TerminalType.NIB.name()),
                (resultSet, rowNumber) -> resultSet.getInt("CHANNEL_ID")
        ).stream().findFirst());
    }

    public Optional<UserChannelAuthenticationRow> findSourceUserChannelAuthentication(
            Integer userId,
            TerminalType sourceTerminal
    ) {
        String sql = """
                SELECT u.*
                FROM %s u
                JOIN %s c ON u.CHANNEL_ID = c.CHANNEL_ID
                WHERE u.USER_ID = :userId AND c.CODE = :sourceCode
                ORDER BY u.USER_CHANNEL_AUTHENTICATION_ID
                """.formatted(USER_CHANNEL_AUTHENTICATION_TABLE, CHANNEL_TABLE);
        return execute("find source user channel authentication", () -> namedParameterJdbcTemplate
                .queryForList(sql, Map.of("userId", userId, "sourceCode", sourceTerminal.name()))
                .stream()
                .map(this::toUserChannelAuthenticationRow)
                .findFirst());
    }

    public boolean nibUserChannelAuthenticationExists(Integer userId) {
        String sql = """
                SELECT COUNT(*)
                FROM REF.USER_CHANNEL_AUTHENTICATION u
                JOIN REF.CHANNEL c ON u.CHANNEL_ID = c.CHANNEL_ID
                WHERE u.USER_ID = :userId AND c.CODE = :targetCode
                """;
        return execute("check existing NIB user channel authentication", () -> {
            Integer count = namedParameterJdbcTemplate.queryForObject(
                    sql,
                    Map.of("userId", userId, "targetCode", TerminalType.NIB.name()),
                    Integer.class
            );
            return count != null && count > 0;
        });
    }

    public void insertUserChannelAuthentication(UserChannelAuthenticationRow row, Integer targetChannelId) {
        if (nibUserChannelAuthenticationExists(row.userId())) {
            throw new NibActivationAlreadyExistsException();
        }
        String sql = """
                INSERT INTO %s (
                    ARCHIVE_NO, CHANNEL_ID, ACTIVE, AUTHENTICATION_METHOD_ID, USER_AUTHENTICATION_TYPE,
                    USER_CHANNEL_AUTHENTICATION_ID, USER_ID, FROM_DATE, TO_DATE, FIRST_PASSWORD, SECOND_PASSWORD,
                    CHANNEL_ACCESS_PARAM, SECOND_LEVEL_AUTH_METHOD_ID, PRINT_COUNT, PASSWORD_SET_PRINTED,
                    CREATED_BY, MODIFIED_BY, CREATION_DATE, MODIFICATION_DATE, EFFECTIVE_DATE, OTP_SERIAL_NO,
                    STATE, NICK_NAME, BRANCH_CODE, PIN_BASED_PASSWORD, PATTERN_BASED_PASSWORD,
                    LAST_DATE_OF_PASSWORD_CHANGE, LAST_REACTION_DATE_TO_PASSWORD, ABORT_PASS, USER_REASON,
                    REASON, DE_ACTIVE_REASON
                ) VALUES (
                    :archiveNo, :channelId, :active, :authenticationMethodId, :userAuthenticationType,
                    :id, :userId, :fromDate, :toDate, :firstPassword, :secondPassword,
                    :channelAccessParameter, :secondLevelAuthenticationMethodId, :printCount, :passwordSetPrinted,
                    :createdBy, :modifiedBy, :creationDate, :modificationDate, :effectiveDate, :otpSerialNumber,
                    :state, :nickname, :branchCode, :pinBasedPassword, :patternBasedPassword,
                    :lastPasswordChangeDate, :lastPasswordReactionDate, :abortPassword, :userReason,
                    :reason, :deactivationReason
                )
                """.formatted(USER_CHANNEL_AUTHENTICATION_TABLE);
        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("archiveNo", row.archiveNo())
                .addValue("channelId", targetChannelId)
                .addValue("active", row.active())
                .addValue("authenticationMethodId", row.authenticationMethodId())
                .addValue("userAuthenticationType", row.userAuthenticationType())
                .addValue("id", nextSequenceValue("REF.SQUSERCHANNELAUTHENTICATION"))
                .addValue("userId", row.userId())
                .addValue("fromDate", row.fromDate())
                .addValue("toDate", row.toDate())
                .addValue("firstPassword", row.firstPassword())
                .addValue("secondPassword", row.secondPassword())
                .addValue("channelAccessParameter", row.channelAccessParameter())
                .addValue("secondLevelAuthenticationMethodId", row.secondLevelAuthenticationMethodId())
                .addValue("printCount", row.printCount())
                .addValue("passwordSetPrinted", row.passwordSetPrinted())
                .addValue("createdBy", row.createdBy())
                .addValue("modifiedBy", row.modifiedBy())
                .addValue("creationDate", row.creationDate())
                .addValue("modificationDate", row.modificationDate())
                .addValue("effectiveDate", row.effectiveDate())
                .addValue("otpSerialNumber", row.otpSerialNumber())
                .addValue("state", row.state())
                .addValue("nickname", row.nickname())
                .addValue("branchCode", row.branchCode())
                .addValue("pinBasedPassword", row.pinBasedPassword())
                .addValue("patternBasedPassword", row.patternBasedPassword())
                .addValue("lastPasswordChangeDate", row.lastPasswordChangeDate())
                .addValue("lastPasswordReactionDate", row.lastPasswordReactionDate())
                .addValue("abortPassword", row.abortPassword())
                .addValue("userReason", row.userReason())
                .addValue("reason", row.reason())
                .addValue("deactivationReason", row.deactivationReason());
        executeUpdate("insert NIB user channel authentication", () -> namedParameterJdbcTemplate.update(sql, parameters));
    }

    public List<NibChannelMapping> findNibChannelMappings() {
        String sql = """
                SELECT AUTHENTICATION_METHOD_ID, CHANNEL_ID
                FROM REF.CHANNEL
                WHERE CODE = :code
                """;
        return execute("find NIB channel mappings", () -> namedParameterJdbcTemplate.queryForList(
                sql,
                Map.of("code", TerminalType.NIB.name())
        ).stream().map(row -> new NibChannelMapping(
                integer(row, "AUTHENTICATION_METHOD_ID"),
                integer(row, "CHANNEL_ID")
        )).toList());
    }

    public List<MembershipChannelAccessRow> findSourceMembershipChannelAccess(
            Integer userId,
            TerminalType sourceTerminal
    ) {
        String sql = """
                SELECT mca.*, c.AUTHENTICATION_METHOD_ID
                FROM REF.MEMBERSHIP m
                JOIN REF.MEMBERSHIP_CHANNEL_ACCESS mca ON m.MEMBERSHIP_ID = mca.MEMBERSHIP_ID
                JOIN REF.CHANNEL c ON mca.CHANNEL_ID = c.CHANNEL_ID
                WHERE m.USER_ID = :userId AND c.CODE = :sourceCode
                ORDER BY mca.MEMBERSHIP_ID, mca.MEMBERSHIP_CHANNEL_ACCESS_ID
                """;
        return execute("find source membership channel access", () -> namedParameterJdbcTemplate.queryForList(
                sql,
                Map.of("userId", userId, "sourceCode", sourceTerminal.name())
        ).stream().map(this::toMembershipChannelAccessRow).toList());
    }

    public Integer insertMembershipChannelAccess(MembershipChannelAccessRow row, Integer targetChannelId) {
        String sql = """
                INSERT INTO REF.MEMBERSHIP_CHANNEL_ACCESS (
                    MEMBERSHIP_CHANNEL_ACCESS_ID, CHANNEL_ID, MEMBERSHIP_ID, MAX_WITHDRAWAL_PER_DAY,
                    ACTIVE, FROM_DATE, TO_DATE, MAX_PERS_WITHDRAWAL_PER_DAY, USER_REASON,
                    REASON, DE_ACTIVE_REASON, FAVORITE
                ) VALUES (
                    :id, :channelId, :membershipId, :maxWithdrawalPerDay,
                    :active, :fromDate, :toDate, :maxPersonalWithdrawalPerDay, :userReason,
                    :reason, :deactivationReason, :favorite
                )
                """;
        Integer id = nextSequenceValue("REF.SQMEMBERSHIPEBACCESS");
        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("channelId", targetChannelId)
                .addValue("membershipId", row.membershipId())
                .addValue("maxWithdrawalPerDay", row.maxWithdrawalPerDay())
                .addValue("active", row.active())
                .addValue("fromDate", row.fromDate())
                .addValue("toDate", row.toDate())
                .addValue("maxPersonalWithdrawalPerDay", row.maxPersonalWithdrawalPerDay())
                .addValue("userReason", row.userReason())
                .addValue("reason", row.reason())
                .addValue("deactivationReason", row.deactivationReason())
                .addValue("favorite", row.favorite());
        executeUpdate("insert NIB membership channel access", () -> namedParameterJdbcTemplate.update(sql, parameters));
        return id;
    }

    public List<MembershipChannelServiceAccessRow> findMembershipChannelServiceAccess(Integer membershipAccessId) {
        String sql = """
                SELECT *
                FROM REF.MEMBERSHIP_CHANNEL_SERVICE_ACCESS
                WHERE MCS_ID = :membershipAccessId
                ORDER BY MCSAS_ID
                """;
        return execute("find membership channel service access", () -> namedParameterJdbcTemplate.queryForList(
                sql,
                Map.of("membershipAccessId", membershipAccessId)
        ).stream().map(this::toMembershipChannelServiceAccessRow).toList());
    }

    public Optional<BigDecimal> findTargetChannelServiceAccessId(
            BigDecimal sourceChannelServiceAccessId,
            Integer targetChannelId
    ) {
        String serviceSql = """
                SELECT EB_SERVICE_ID
                FROM REF.CHANNEL_SERVICE_ACCESS
                WHERE CHANNEL_SERVICE_ACCESS_ID = :sourceAccessId
                """;
        return execute("find target channel service access", () -> {
            List<Integer> serviceIds = namedParameterJdbcTemplate.query(
                    serviceSql,
                    Map.of("sourceAccessId", sourceChannelServiceAccessId),
                    (resultSet, rowNumber) -> resultSet.getInt("EB_SERVICE_ID")
            );
            if (serviceIds.isEmpty()) {
                return Optional.empty();
            }
            String targetSql = """
                    SELECT CHANNEL_SERVICE_ACCESS_ID
                    FROM REF.CHANNEL_SERVICE_ACCESS
                    WHERE CHANNEL_ID = :targetChannelId AND EB_SERVICE_ID = :serviceId
                    """;
            return namedParameterJdbcTemplate.query(
                    targetSql,
                    Map.of("targetChannelId", targetChannelId, "serviceId", serviceIds.getFirst()),
                    (resultSet, rowNumber) -> resultSet.getBigDecimal("CHANNEL_SERVICE_ACCESS_ID")
            ).stream().findFirst();
        });
    }

    public Integer insertMembershipChannelServiceAccess(
            MembershipChannelServiceAccessRow source,
            Integer targetMembershipAccessId,
            BigDecimal targetChannelServiceAccessId
    ) {
        String sql = """
                INSERT INTO REF.MEMBERSHIP_CHANNEL_SERVICE_ACCESS (
                    ARCHIVE_NO, MCSAS_ID, MCS_ID, MAX_WITHDRAWAL_PER_TRANSACTION, CHANNEL_EB_ACCESS_ID
                ) VALUES (?, ?, ?, ?, ?)
                """;
        Integer id = nextSequenceValue("REF.SQMCSAS");
        executeUpdate("insert NIB membership channel service access", () -> jdbcTemplate.update(
                sql,
                source.archiveNo(),
                id,
                targetMembershipAccessId,
                source.maxWithdrawalPerTransaction(),
                targetChannelServiceAccessId
        ));
        return id;
    }

    public List<NibRoleRow> findRoles(Set<String> roleCodes) {
        String sql = "SELECT CODE, ROLE_ID FROM REF.ROLE WHERE CODE IN (:roleCodes)";
        return execute("find NIB activation roles", () -> namedParameterJdbcTemplate.queryForList(
                sql,
                Map.of("roleCodes", roleCodes)
        ).stream().map(row -> new NibRoleRow(
                String.valueOf(row.get("CODE")),
                number(row, "ROLE_ID").longValue()
        )).toList());
    }

    public boolean userHasRole(Integer personId, Long roleId) {
        String sql = "SELECT COUNT(*) FROM REF.USERROLE WHERE USER_ID = :personId AND ROLE_ID = :roleId";
        return execute("check NIB activation user role", () -> {
            Integer count = namedParameterJdbcTemplate.queryForObject(
                    sql,
                    Map.of("personId", personId, "roleId", roleId),
                    Integer.class
            );
            return count != null && count > 0;
        });
    }

    public void insertUserRole(Integer personId, Long roleId) {
        String sql = "INSERT INTO REF.USERROLE (ROLE_ID, USER_ID) VALUES (:roleId, :personId)";
        executeUpdate("insert NIB activation user role", () -> namedParameterJdbcTemplate.update(
                sql,
                Map.of("roleId", roleId, "personId", personId)
        ));
    }

    private Integer nextSequenceValue(String sequenceName) {
        return execute("read NIB activation sequence", () -> {
            Integer value = jdbcTemplate.queryForObject(
                    "SELECT NEXT VALUE FOR " + sequenceName + " FROM SYSIBM.SYSDUMMY1",
                    Integer.class
            );
            if (value == null) {
                throw new NibActivationDataAccessException("read NIB activation sequence");
            }
            return value;
        });
    }

    private UserChannelAuthenticationRow toUserChannelAuthenticationRow(Map<String, Object> row) {
        return new UserChannelAuthenticationRow(
                row.get("ARCHIVE_NO"), integer(row, "CHANNEL_ID"), row.get("ACTIVE"),
                integer(row, "AUTHENTICATION_METHOD_ID"), row.get("USER_AUTHENTICATION_TYPE"),
                integer(row, "USER_ID"), row.get("FROM_DATE"), row.get("TO_DATE"),
                row.get("FIRST_PASSWORD"), row.get("SECOND_PASSWORD"), row.get("CHANNEL_ACCESS_PARAM"),
                row.get("SECOND_LEVEL_AUTH_METHOD_ID"), row.get("PRINT_COUNT"), row.get("PASSWORD_SET_PRINTED"),
                row.get("CREATED_BY"), row.get("MODIFIED_BY"), row.get("CREATION_DATE"),
                row.get("MODIFICATION_DATE"), row.get("EFFECTIVE_DATE"), row.get("OTP_SERIAL_NO"),
                row.get("STATE"), row.get("NICK_NAME"), row.get("BRANCH_CODE"), row.get("PIN_BASED_PASSWORD"),
                row.get("PATTERN_BASED_PASSWORD"), row.get("LAST_DATE_OF_PASSWORD_CHANGE"),
                row.get("LAST_REACTION_DATE_TO_PASSWORD"), row.get("ABORT_PASS"), row.get("USER_REASON"),
                row.get("REASON"), row.get("DE_ACTIVE_REASON")
        );
    }

    private MembershipChannelAccessRow toMembershipChannelAccessRow(Map<String, Object> row) {
        return new MembershipChannelAccessRow(
                integer(row, "MEMBERSHIP_CHANNEL_ACCESS_ID"), integer(row, "CHANNEL_ID"),
                integer(row, "MEMBERSHIP_ID"), integer(row, "AUTHENTICATION_METHOD_ID"),
                row.get("MAX_WITHDRAWAL_PER_DAY"), row.get("ACTIVE"), row.get("FROM_DATE"), row.get("TO_DATE"),
                row.get("MAX_PERS_WITHDRAWAL_PER_DAY"), row.get("USER_REASON"), row.get("REASON"),
                row.get("DE_ACTIVE_REASON"), row.get("FAVORITE")
        );
    }

    private MembershipChannelServiceAccessRow toMembershipChannelServiceAccessRow(Map<String, Object> row) {
        return new MembershipChannelServiceAccessRow(
                row.get("ARCHIVE_NO"), integer(row, "MCSAS_ID"), integer(row, "MCS_ID"),
                row.get("MAX_WITHDRAWAL_PER_TRANSACTION"), bigDecimal(row, "CHANNEL_EB_ACCESS_ID")
        );
    }

    private Integer integer(Map<String, Object> row, String column) {
        return number(row, column).intValue();
    }

    private Number number(Map<String, Object> row, String column) {
        Object value = row.get(column);
        if (value instanceof Number number) {
            return number;
        }
        throw new NibActivationDataAccessException("map required numeric column " + column);
    }

    private BigDecimal bigDecimal(Map<String, Object> row, String column) {
        Number value = number(row, column);
        return value instanceof BigDecimal decimal ? decimal : BigDecimal.valueOf(value.longValue());
    }

    private void executeUpdate(String operation, Supplier<Integer> update) {
        Integer affectedRows;
        try {
            affectedRows = update.get();
        } catch (NibActivationAlreadyExistsException | NibActivationDataAccessException exception) {
            throw exception;
        } catch (DataAccessException exception) {
            if (exception instanceof DuplicateKeyException || hasSqlState(exception, "23505")) {
                throw new NibActivationAlreadyExistsException(exception);
            }
            throw new NibActivationDataAccessException(operation, exception);
        }
        if (affectedRows == null || affectedRows != 1) {
            throw new NibActivationDataAccessException(operation + " (expected one affected row)");
        }
    }

    private <T> T execute(String operation, Supplier<T> action) {
        try {
            return action.get();
        } catch (NibActivationAlreadyExistsException | NibActivationDataAccessException exception) {
            throw exception;
        } catch (DataAccessException exception) {
            throw new NibActivationDataAccessException(operation, exception);
        }
    }

    private boolean hasSqlState(Throwable throwable, String sqlState) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof SQLException sqlException && sqlState.equals(sqlException.getSQLState())) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }
}
