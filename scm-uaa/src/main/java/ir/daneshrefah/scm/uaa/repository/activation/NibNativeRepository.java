package ir.daneshrefah.scm.uaa.repository.activation;

import ir.daneshrefah.scm.common.constant.TerminalType;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
@Repository
@Slf4j
public class NibNativeRepository {

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private static final String USER_CHANNEL_AUTHENTICATION_TABLE = "REF.USER_CHANNEL_AUTHENTICATION";
    private static final String CHANNEL_TABLE = "REF.CHANNEL";
    public static final String AUTHENTICATION_METHOD_ID_NAME = "AUTHENTICATION_METHOD_ID";
    private static final String CHANNEL_ID_NAME = "CHANNEL_ID";


    /**
     * Finds the parent NIB channel ID where PARENT_ID is null.
     *
     * @return The CHANNEL_ID or null if not found.
     * @throws DataAccessException if a database error occurs.
     */
    public Integer findParentChannel(TerminalType fromTerminal) {
        String sql = "SELECT CHANNEL_ID FROM " + CHANNEL_TABLE + " WHERE CODE = :code AND PARENT_ID IS NULL";
        Map<String, Object> params = Collections.singletonMap("code",fromTerminal.name());
        try {
            return namedParameterJdbcTemplate.queryForObject(sql, params, Integer.class);
        } catch (DataAccessException e) {
            log.error("Failed to find parent NIB channel: %s".formatted( e.getMessage()),e);
            return null; // Or throw a custom exception depending on your needs
        }
    }


    /**
     * Fetches USER_CHANNEL_AUTHENTICATION records for a given userId, joined with CHANNEL table,
     * where the channel CODE is either 'IB' or 'CIB'.
     *
     * @param userId       The user ID to filter by.
     * @param fromTerminal The terminal code (not used in filtering here, included for future flexibility).
     * @return List of records as Maps, empty if none found.
     * @throws DataAccessException if a database error occurs.
     */
    public List<Map<String, Object>> findUserChannelAuthenticationByUserId(Long userId, TerminalType fromTerminal) {
        String sql = """
            SELECT u.*, c.CODE\s
            FROM %s u\s
            JOIN %s c ON u.CHANNEL_ID = c.CHANNEL_ID\s
            WHERE u.USER_ID = :userId AND c.CODE IN (:codes)
           \s""".formatted(USER_CHANNEL_AUTHENTICATION_TABLE, CHANNEL_TABLE);


        Map<String, Object> params = Map.of(
                "userId", userId,
                "codes", List.of(TerminalType.IB.name(), TerminalType.CIB.name())
        );
        try {
            return namedParameterJdbcTemplate.queryForList(sql, params);
        } catch (DataAccessException e) {
            log.error("Failed to fetch USER_CHANNEL_AUTHENTICATION for userId %s: %s".formatted(userId,  e.getMessage()), e);
            return Collections.emptyList(); // Safe default return
        }
    }

    /**
     * Retrieves the maximum USER_CHANNEL_AUTHENTICATION_ID.
     *
     * @return The maximum ID or null if the table is empty.
     * @throws DataAccessException if a database error occurs.
     */
//    public Long findUserChannelAuthenticationMaxId() {
//        return findMaxId("USER_CHANNEL_AUTHENTICATION_ID", USER_CHANNEL_AUTHENTICATION_TABLE);
//    }
    /**
     * Generic method to find the maximum value of an ID column in a table.
     *
     * @param idColumnName The name of the ID column (e.g., "USER_CHANNEL_AUTHENTICATION_ID").
     * @param tableName    The table name, including schema prefix if needed (e.g., "REF.USER_CHANNEL_AUTHENTICATION").
     * @return The maximum ID value, or 0 if the table is empty or no value is found.
     * @throws DataAccessException if a database error occurs that cannot be recovered from.
     */
//    public Long findMaxId(String idColumnName, String tableName) {
//        String sql = "SELECT MAX(" + idColumnName + ") FROM " + tableName;
//        log.debug("Executing query to find max ID: {}", sql);
//
//        try {
//            Long result = jdbcTemplate.queryForObject(sql, Long.class);
//            return result != null ? result : 0L; // Return 0 if table is empty or result is null
//        } catch (DataAccessException e) {
//            log.error("Failed to find max ID for column '{}' in table '{}': {}",
//                    idColumnName, tableName, e.getMessage());
//            throw e; // Re-throw the exception to let the caller handle it
//        }
//    }

    /**
     * Inserts a new USER_CHANNEL_AUTHENTICATION record.
     *
     * @param userChannelAuth Map containing all required fields.
     * @return Number of rows affected (should be 1 if successful).
     * @throws IllegalArgumentException if required fields are missing.
     * @throws DataAccessException      if a database error occurs.
     */
    public int insertUserChannelAuthentication(Map<String, Object> userChannelAuth) {
        String sql = """
        INSERT INTO %s (
            ARCHIVE_NO, CHANNEL_ID, ACTIVE, AUTHENTICATION_METHOD_ID, USER_AUTHENTICATION_TYPE, USER_CHANNEL_AUTHENTICATION_ID,
            USER_ID, FROM_DATE, TO_DATE, FIRST_PASSWORD, SECOND_PASSWORD,
            CHANNEL_ACCESS_PARAM, SECOND_LEVEL_AUTH_METHOD_ID, PRINT_COUNT, PASSWORD_SET_PRINTED,
            CREATED_BY, MODIFIED_BY, CREATION_DATE, MODIFICATION_DATE, EFFECTIVE_DATE, OTP_SERIAL_NO,
            STATE, NICK_NAME, BRANCH_CODE, PIN_BASED_PASSWORD, PATTERN_BASED_PASSWORD,
            LAST_DATE_OF_PASSWORD_CHANGE, LAST_REACTION_DATE_TO_PASSWORD, ABORT_PASS, USER_REASON,
            REASON, DE_ACTIVE_REASON
        ) VALUES (
            :archiveNo, :channelId, :active, :authenticationMethodId, :userAuthenticationType,
            :userChannelAuthenticationId, :userId, :fromDate, :toDate, :firstPassword, :secondPassword,
            :channelAccessParam, :secondLevelAuthMethodId, :printCount, :passwordSetPrinted,
            :createdBy, :modifiedBy, :creationDate, :modificationDate, :effectiveDate, :otpSerialNo,
            :state, :nickName, :branchCode, :pinBasedPassword, :patternBasedPassword,
            :lastDateOfPasswordChange, :lastReactionDateToPassword, :abortPass, :userReason,
            :reason, :deActiveReason
        )
        """.formatted(USER_CHANNEL_AUTHENTICATION_TABLE);

        Integer sequenceValue = getSequenceValue("REF.SQUSERCHANNELAUTHENTICATION");
        Map<String, Object> params = new HashMap<>();
        params.put("archiveNo", userChannelAuth.get("ARCHIVE_NO"));
        params.put("channelId", userChannelAuth.get("CHANNEL_ID"));
        params.put("active", userChannelAuth.get("ACTIVE"));
        params.put("authenticationMethodId", userChannelAuth.get("AUTHENTICATION_METHOD_ID"));
        params.put("userAuthenticationType", userChannelAuth.get("USER_AUTHENTICATION_TYPE"));
        params.put("userChannelAuthenticationId", sequenceValue);
        params.put("userId", userChannelAuth.get("USER_ID"));
        params.put("fromDate", userChannelAuth.get("FROM_DATE"));
        params.put("toDate", userChannelAuth.get("TO_DATE"));
        params.put("firstPassword", userChannelAuth.get("FIRST_PASSWORD"));
        params.put("secondPassword", userChannelAuth.get("SECOND_PASSWORD"));
        params.put("channelAccessParam", userChannelAuth.get("CHANNEL_ACCESS_PARAM"));
        params.put("secondLevelAuthMethodId", userChannelAuth.get("SECOND_LEVEL_AUTH_METHOD_ID"));
        params.put("printCount", userChannelAuth.get("PRINT_COUNT"));
        params.put("passwordSetPrinted", userChannelAuth.get("PASSWORD_SET_PRINTED"));
        params.put("createdBy", userChannelAuth.get("CREATED_BY"));
        params.put("modifiedBy", userChannelAuth.get("MODIFIED_BY"));
        params.put("creationDate", userChannelAuth.get("CREATION_DATE"));
        params.put("modificationDate", userChannelAuth.get("MODIFICATION_DATE"));
        params.put("effectiveDate", userChannelAuth.get("EFFECTIVE_DATE"));
        params.put("otpSerialNo", userChannelAuth.get("OTP_SERIAL_NO"));
        params.put("state", userChannelAuth.get("STATE"));
        params.put("nickName", userChannelAuth.get("NICK_NAME"));
        params.put("branchCode", userChannelAuth.get("BRANCH_CODE"));
        params.put("pinBasedPassword", userChannelAuth.get("PIN_BASED_PASSWORD"));
        params.put("patternBasedPassword", userChannelAuth.get("PATTERN_BASED_PASSWORD"));
        params.put("lastDateOfPasswordChange", userChannelAuth.get("LAST_DATE_OF_PASSWORD_CHANGE"));
        params.put("lastReactionDateToPassword", userChannelAuth.get("LAST_REACTION_DATE_TO_PASSWORD"));
        params.put("abortPass", userChannelAuth.get("ABORT_PASS"));
        params.put("userReason", userChannelAuth.get("USER_REASON"));
        params.put("reason", userChannelAuth.get("REASON"));
        params.put("deActiveReason", userChannelAuth.get("DE_ACTIVE_REASON"));

        try {
            // Optional: Check for duplicates before insertion
            String checkSql = "select count(USER_ID) from ref.USER_CHANNEL_AUTHENTICATION where USER_ID = :userId and CHANNEL_ID in (select CHANNEL_ID from ref.CHANNEL where CODE in('NIB'))";
            int count = namedParameterJdbcTemplate.queryForObject(checkSql, Map.of("userId", userChannelAuth.get("USER_ID")), Integer.class);
            if (count > 0) {
                log.warn("Duplicate USER_ID and CHANNEL_ID found: {}",
                        userChannelAuth.get("USER_ID") + " - " + userChannelAuth.get("CHANNEL_ID"));
                return 1; // Or handle as needed;
            }

            return namedParameterJdbcTemplate.update(sql, params);
        } catch (DataAccessException e) {
            log.error("Failed to insert USER_CHANNEL_AUTHENTICATION: %s".formatted( e.getMessage()),e);
            if (e.getCause() instanceof SQLException sqlEx && sqlEx.getSQLState().equals("23505")) {
                log.error("Duplicate key violation: {}", sqlEx.getMessage());
                throw new DataAccessException("Duplicate entry detected for USER_CHANNEL_AUTHENTICATION", e) {};
            }
            throw e;
        }
    }


    /**
     * Retrieves NIB channel details as a Map with AUTHENTICATION_METHOD_ID as key and CHANNEL_ID as value.
     *
     * @return A Map where keys are AUTHENTICATION_METHOD_ID and values are CHANNEL_ID,
     *         or an empty Map if no records are found or an error occurs.
     */
    public Map<Integer, Integer> findNibChannel() {
        String sql = """
            SELECT c1.AUTHENTICATION_METHOD_ID, c1.CHANNEL_ID \s
            FROM %s c1\s
            WHERE c1.CODE = :code \s
            \s""".formatted(CHANNEL_TABLE);
        Map<String, Object> params = Collections.singletonMap("code", "NIB");
        log.debug("Executing query to find NIB channel: {}", sql);

        try {
            List<Map<String, Object>> results = namedParameterJdbcTemplate.queryForList(sql, params);
            if (results.isEmpty()) {
                log.warn("No NIB channel found for CODE = 'NIB'");
                return Collections.emptyMap();
            }

            Map<Integer, Integer> channelMap = new HashMap<>();
            for (Map<String, Object> row : results) {
                Object authMethodIdObj = row.get(AUTHENTICATION_METHOD_ID_NAME);
                Object channelIdObj = row.get(CHANNEL_ID_NAME);

                if (authMethodIdObj instanceof Integer authMethodId && channelIdObj instanceof Integer channelId) {
                    channelMap.put(authMethodId, channelId);
                } else {
                    log.warn("Skipping invalid row with non-Integer AUTHENTICATION_METHOD_ID or CHANNEL_ID");
                }
            }

            if (channelMap.isEmpty()) {
                log.warn("No valid NIB channel data found");
                return Collections.emptyMap();
            }

            log.info("Found {} NIB channel mappings", channelMap.size());
            return channelMap;
        } catch (DataAccessException e) {
            log.error("Failed to find NIB channel: %s".formatted(e.getMessage()),e);
            return Collections.emptyMap();
        }
    }

    /**
     * Retrieves all MEMBERSHIP_CHANNEL_ACCESS records for a given USER_ID.
     *
     * @param userId       The ID of the user to filter memberships by.
     * @param fromTerminal
     * @return A List of Maps, each containing all columns from MEMBERSHIP_CHANNEL_ACCESS,
     * or an empty List if no records are found or an error occurs.
     */
    public List<Map<String, Object>> findMembershipChannelAccessByUserId(Long userId, TerminalType fromTerminal) {
        String sql = """
            SELECT mca.*,c.AUTHENTICATION_METHOD_ID\s
            FROM ref.MEMBERSHIP m\s
            JOIN ref.MEMBERSHIP_CHANNEL_ACCESS mca ON m.MEMBERSHIP_ID = mca.MEMBERSHIP_ID\s
            JOIN ref.CHANNEL c ON mca.CHANNEL_ID = c.CHANNEL_ID\s
            WHERE c.CODE IN ('IB', 'CIB') and  m.USER_ID = :userId order by mca.MEMBERSHIP_ID  ,c.CODE %s \s
            """.formatted(fromTerminal == TerminalCodes.IB ? "desc" : "asc");
        log.debug("SQL Query: {}", sql);
        Map<String, Object> params = Collections.singletonMap("userId", userId);
        log.debug("Executing query to find MEMBERSHIP_CHANNEL_ACCESS for userId {}: {}", userId, sql);

        try {
            List<Map<String, Object>> results = namedParameterJdbcTemplate.queryForList(sql, params);
            if (results.isEmpty()) {
                log.warn("No MEMBERSHIP_CHANNEL_ACCESS records found for userId {}", userId);
            } else {
                log.info("Found {} MEMBERSHIP_CHANNEL_ACCESS records for userId {}", results.size(), userId);
            }
            return results;
        } catch (DataAccessException e) {
            log.error("Failed to fetch MEMBERSHIP_CHANNEL_ACCESS for userId %s: %s".formatted(userId, e.getMessage()),e);
            return Collections.emptyList();
        }
    }


    /**
     * Inserts a new MEMBERSHIP_CHANNEL_ACCESS record.
     *
     * @param record The record to insert as a Map of column names to values.
     * @throws DataAccessException if insertion fails.
     */
    public Integer insertMembershipChannelAccess(Map<String, Object> record) {
        String sql = """
            INSERT INTO ref.MEMBERSHIP_CHANNEL_ACCESS (
                MEMBERSHIP_CHANNEL_ACCESS_ID, CHANNEL_ID, MEMBERSHIP_ID, MAX_WITHDRAWAL_PER_DAY, 
                ACTIVE, FROM_DATE, TO_DATE, MAX_PERS_WITHDRAWAL_PER_DAY, USER_REASON, 
                REASON, DE_ACTIVE_REASON, FAVORITE
            ) VALUES (
                :membershipChannelAccessId, :channelId, :membershipId, :maxWithdrawalPerDay, 
                :active, :fromDate, :toDate, :maxPersWithdrawalPerDay, :userReason, 
                :reason, :deActiveReason, :favorite
            )
            """;
        Integer sequenceValue = getSequenceValue("REF.SQMEMBERSHIPEBACCESS");
        Map<String, Object> params = new HashMap<>();
        params.put("membershipChannelAccessId", sequenceValue);
        params.put("channelId", record.get("CHANNEL_ID"));
        params.put("membershipId", record.get("MEMBERSHIP_ID"));
        params.put("maxWithdrawalPerDay", record.get("MAX_WITHDRAWAL_PER_DAY"));
        params.put("active", record.get("ACTIVE"));
        params.put("fromDate", record.get("FROM_DATE"));
        params.put("toDate", record.get("TO_DATE"));
        params.put("maxPersWithdrawalPerDay", record.get("MAX_PERS_WITHDRAWAL_PER_DAY"));
        params.put("userReason", record.get("USER_REASON"));
        params.put("reason", record.get("REASON"));
        params.put("deActiveReason", record.get("DE_ACTIVE_REASON"));
        params.put("favorite", record.get("FAVORITE"));

        try {
            int rowsAffected = namedParameterJdbcTemplate.update(sql, params);
            if (rowsAffected != 1) {
                log.error("Failed to insert MEMBERSHIP_CHANNEL_ACCESS record: {} rows affected", rowsAffected);
                throw new IllegalStateException("Expected 1 row to be inserted, but got " + rowsAffected);
            }
        } catch (DataAccessException e) {
            log.error("Failed to insert MEMBERSHIP_CHANNEL_ACCESS record: %s".formatted(e.getMessage()),e);
            throw e;
        }
        return sequenceValue;
    }

    public   List<Map<String, Object>> getMcsasRecords(Integer oldMcsId) {
        // Fetch existing MEMBERSHIP_CHANNEL_SERVICE_ACCESS records
        String sql = """
        SELECT *\s
        FROM ref.MEMBERSHIP_CHANNEL_SERVICE_ACCESS\s
        WHERE MCS_ID = :mcsId \s
        """;
        Map<String, Object> params = Collections.singletonMap("mcsId", oldMcsId);
        List<Map<String, Object>> mcsasRecords;
        try {
            mcsasRecords = namedParameterJdbcTemplate.queryForList(sql, params);
        } catch (DataAccessException e) {
            log.error("Failed to fetch MEMBERSHIP_CHANNEL_SERVICE_ACCESS for MCS_ID %s: %s".formatted(oldMcsId, e.getMessage()),e);
            return null;
        }
        return mcsasRecords;
    }

    /**
     * Finds a new CHANNEL_EB_ACCESS_ID corresponding to the new CHANNEL_ID, based on the old CHANNEL_EB_ACCESS_ID.
     *
     * @param oldChannelEbAccessId The original CHANNEL_EB_ACCESS_ID.
     * @param oldChannelId         The original CHANNEL_ID.
     * @param newChannelId         The new CHANNEL_ID.
     * @return The new CHANNEL_EB_ACCESS_ID, or null if not found.
     */
    public BigDecimal findNewChannelEbAccessId(BigDecimal oldChannelEbAccessId, Integer oldChannelId, Integer newChannelId) {
        // Fetch the EB_SERVICE_ID for the old CHANNEL_EB_ACCESS_ID
        String sql = """
        SELECT EB_SERVICE_ID \s
        FROM ref.CHANNEL_SERVICE_ACCESS \s
        WHERE CHANNEL_SERVICE_ACCESS_ID = :channelServiceAccessId
        """;
        Map<String, Object> params = Collections.singletonMap("channelServiceAccessId", oldChannelEbAccessId);
        Integer ebServiceId;
        try {
            ebServiceId = namedParameterJdbcTemplate.queryForObject(sql, params, Integer.class);
        } catch (DataAccessException e) {
            log.error("Failed to fetch EB_SERVICE_ID for CHANNEL_EB_ACCESS_ID %s: %s".formatted(oldChannelEbAccessId, e.getMessage()),e);
            return null;
        }

        // Find a CHANNEL_SERVICE_ACCESS record for the new CHANNEL_ID with the same EB_SERVICE_ID
        sql = """
        SELECT CHANNEL_SERVICE_ACCESS_ID\s
        FROM ref.CHANNEL_SERVICE_ACCESS\s
        WHERE CHANNEL_ID = :channelId AND EB_SERVICE_ID = :ebServiceId
        """;
        params = Map.of(
                "channelId", newChannelId,
                "ebServiceId", ebServiceId
        );
        try {
            return namedParameterJdbcTemplate.queryForObject(sql, params, BigDecimal.class);
        } catch (DataAccessException e) {
            log.error("No CHANNEL_SERVICE_ACCESS found for CHANNEL_ID %s and EB_SERVICE_ID %s: %s".formatted(
                    newChannelId, ebServiceId, e.getMessage()),e);
            return null;
        }
    }

    /**
     * Inserts a new MEMBERSHIP_CHANNEL_SERVICE_ACCESS record.
     *
     * @param record The record to insert as a Map of column names to values.
     */
    public Integer insertMembershipChannelServiceAccess(Map<String, Object> record) {
        Integer mcsasId = getSequenceValue("REF.SQMCSAS");

        String sql = """
        INSERT INTO ref.MEMBERSHIP_CHANNEL_SERVICE_ACCESS (
            ARCHIVE_NO, MCSAS_ID, MCS_ID, MAX_WITHDRAWAL_PER_TRANSACTION, CHANNEL_EB_ACCESS_ID
        ) VALUES (
            ?, ?, ?, ?, ?
        )
        """;
        try {
            Object[] objects = new Object[] {
                    record.get("ARCHIVE_NO"),
                    mcsasId,
                    record.get("MCS_ID"),
                    record.get("MAX_WITHDRAWAL_PER_TRANSACTION"),
                    record.get("CHANNEL_EB_ACCESS_ID")
            };
            int rowsAffected = jdbcTemplate.update(sql, objects);
            if (rowsAffected != 1) {
                log.error("Failed to insert MEMBERSHIP_CHANNEL_SERVICE_ACCESS: {} rows affected", rowsAffected);
                throw new IllegalStateException("Expected 1 row to be inserted, but got " + rowsAffected);
            } else {
                log.info("Inserted MEMBERSHIP_CHANNEL_SERVICE_ACCESS record with MCSAS_ID: {}", mcsasId);
                return mcsasId;
            }
        } catch (DataAccessException e) {
            log.error("Failed to insert MEMBERSHIP_CHANNEL_SERVICE_ACCESS: %s".formatted(e.getMessage()),e);
            throw e;
        }
    }

    /**
     * Retrieves the next value for a given sequence.
     * @param sequenceName The name of the sequence to fetch the next value from.
     * @return The next value of the sequence as an Integer.
     */
    private Integer getSequenceValue(String sequenceName) {
        String sequenceSql = "SELECT NEXT VALUE FOR %s FROM SYSIBM.SYSDUMMY1".formatted(sequenceName);
        try {
            return jdbcTemplate.queryForObject(sequenceSql, Integer.class);
        } catch (DataAccessException e) {
            log.error("Failed to fetch sequence value for %s: %s".formatted(sequenceName, e.getMessage()),e);
            return null; // Or handle it as per your application's requirements
        }
    }

    /**
     * Checks if a CHANNEL_ID exists in the CHANNEL table.
     *
     * @param channelId The CHANNEL_ID to check.
     * @return true if the CHANNEL_ID exists, false otherwise.
     */
    private boolean channelExists(Integer channelId) {
        String sql = "SELECT COUNT(*) FROM ref.CHANNEL WHERE CHANNEL_ID = :channelId";
        try {
            return namedParameterJdbcTemplate.queryForObject(sql, Map.of("channelId", channelId), Integer.class) > 0;
        } catch (DataAccessException e) {
            log.error("Failed to check existence of CHANNEL_ID {}: {}", channelId, e.getMessage());
            return false;
        }
    }
    /**
     * Extracts parameters from the map in the correct order for the INSERT statement.
     *
     * @param userChannelAuth Map of field names to values.
     * @return Array of parameter values in the correct order.
     * @throws IllegalArgumentException if a required field is missing.
     */

    private Object[] extractUserChannelAuthenticationParameters(Map<String, Object> userChannelAuth) {
        String[] requiredFields = {
                "ARCHIVE_NO", "CHANNEL_ID", "ACTIVE", "AUTHENTICATION_METHOD_ID", "USER_AUTHENTICATION_TYPE",
                "USER_ID", "FROM_DATE", "TO_DATE", "FIRST_PASSWORD", "SECOND_PASSWORD",
                "CHANNEL_ACCESS_PARAM", "SECOND_LEVEL_AUTH_METHOD_ID", "PRINT_COUNT", "PASSWORD_SET_PRINTED",
                "CREATED_BY", "MODIFIED_BY", "CREATION_DATE", "MODIFICATION_DATE", "EFFECTIVE_DATE", "OTP_SERIAL_NO",
                "STATE", "NICK_NAME", "BRANCH_CODE", "PIN_BASED_PASSWORD", "PATTERN_BASED_PASSWORD",
                "LAST_DATE_OF_PASSWORD_CHANGE", "LAST_REACTION_DATE_TO_PASSWORD", "ABORT_PASS", "USER_REASON",
                "REASON", "DE_ACTIVE_REASON"
        };

        Object[] params = new Object[requiredFields.length];
        for (int i = 0; i < requiredFields.length; i++) {
            if (!userChannelAuth.containsKey(requiredFields[i])) {
                throw new IllegalArgumentException("Missing required field: " + requiredFields[i]);
            }
            params[i] = userChannelAuth.get(requiredFields[i]);
        }
        return params;
    }
    private Object[] extractMembershipServiceChannelAccessParameters(Map<String, Object> map) {
        String[] requiredFields = {
                "ARCHIVE_NO", "MCSAS_ID", "MCS_ID", "MAX_WITHDRAWAL_PER_TRANSACTION", "CHANNEL_EB_ACCESS_ID"
        };

        Object[] params = new Object[requiredFields.length];
        for (int i = 0; i < requiredFields.length; i++) {
            if (!map.containsKey(requiredFields[i])) {
                throw new IllegalArgumentException("Missing required field: " + requiredFields[i]);
            }
            params[i] = map.get(requiredFields[i]);
        }
        return params;
    }

}