package ir.daneshrefah.scm.core.integration.inbound.rest;

import ir.daneshrefah.scm.common.model.message.MessageStatus;

import java.util.HashMap;
import java.util.Map;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-03
 */
public class HttpStatusMapper {

    private static final String DEFAULT_CONTENT_TYPE = "application/json";

    private static final Map<MessageStatus, Integer> statusMappingMap = new HashMap<>();
    static {
        statusMappingMap.put(MessageStatus.SC_PROCESSING, 500);
        statusMappingMap.put(MessageStatus.SC_SUCCESS, 200);
        statusMappingMap.put(MessageStatus.SC_ACCESS_DENIED, 403);
        statusMappingMap.put(MessageStatus.SC_UNAUTHORIZED, 401);
        statusMappingMap.put(MessageStatus.SC_NOT_FOUND, 404);
        statusMappingMap.put(MessageStatus.SC_ERROR_VALIDATION, 400);
        statusMappingMap.put(MessageStatus.SC_ERROR_DATA_INTEGRITY_VIOLATION, 400);
        statusMappingMap.put(MessageStatus.SC_ERROR_SYSTEM, 500);
        statusMappingMap.put(MessageStatus.SC_ERROR_BUSINESS, 400);
        statusMappingMap.put(MessageStatus.SC_ERROR_UNREACHABLE_PROVIDER, 502);
    }

    public static Integer toHttpStatus(MessageStatus status) {
        return statusMappingMap.get(status);
    }

}
