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

        // Success
        statusMappingMap.put(MessageStatus.SC_SUCCESS, 200);
        statusMappingMap.put(MessageStatus.SUCCESSFULLY_PROCESSED, 200);
        statusMappingMap.put(MessageStatus.BALANCE_REQUEST, 200);
        statusMappingMap.put(MessageStatus.REVERSAL_ACCEPTED, 200);
        statusMappingMap.put(MessageStatus.RECONCILIATION_DONE, 200);


        // Authentication & Authorization
        statusMappingMap.put(MessageStatus.SC_UNAUTHORIZED, 401);
        statusMappingMap.put(MessageStatus.SC_ACCESS_DENIED, 403);

        // Not Found
        statusMappingMap.put(MessageStatus.SC_NOT_FOUND, 404);
        statusMappingMap.put(MessageStatus.RECORD_NOT_FOUND, 404);
        statusMappingMap.put(MessageStatus.NO_SUCH_CARD, 404);
        statusMappingMap.put(MessageStatus.UNKNOWN_CARD, 404);
        statusMappingMap.put(MessageStatus.ISSUER_NOT_FOUND, 404);
        statusMappingMap.put(MessageStatus.KARPARDAZ_NOT_FOUND, 404);

        // Validation / Business Error
        statusMappingMap.put(MessageStatus.SC_ERROR_VALIDATION, 400);
        statusMappingMap.put(MessageStatus.SC_ERROR_BUSINESS, 400);
        statusMappingMap.put(MessageStatus.SC_ERROR_DATA_INTEGRITY_VIOLATION, 400);

        statusMappingMap.put(MessageStatus.INVALID_SOURCE_ACCOUNT, 400);
        statusMappingMap.put(MessageStatus.INVALID_EXPIRE_DATE, 400);
        statusMappingMap.put(MessageStatus.INVALID_NATIONAL_CODE, 400);
        statusMappingMap.put(MessageStatus.INVALID_INSDEL, 400);
        statusMappingMap.put(MessageStatus.INVALID_CARD_NO, 400);
        statusMappingMap.put(MessageStatus.INVALID_AMOUNT, 400);
        statusMappingMap.put(MessageStatus.INVALID_CARD_NUMBER, 400);
        statusMappingMap.put(MessageStatus.INVALID_BILLER_ID, 400);
        statusMappingMap.put(MessageStatus.INVALID_DATE, 400);
        statusMappingMap.put(MessageStatus.INVALID_REVERSAL_AMOUNT, 400);
        statusMappingMap.put(MessageStatus.INVALID_TRANSACTION, 400);
        statusMappingMap.put(MessageStatus.INVALID_PHONE_NUMBER, 400);

        statusMappingMap.put(MessageStatus.EMPTY_PRIVILEGES, 400);
        statusMappingMap.put(MessageStatus.EMPTY_PERMIT_SERVICE_ID, 400);
        statusMappingMap.put(MessageStatus.EMPTY_CUSTOMER_NO, 400);

        statusMappingMap.put(MessageStatus.BAD_CVV, 400);
        statusMappingMap.put(MessageStatus.BAD_MERCHANT, 400);
        statusMappingMap.put(MessageStatus.INCORRECT_PIN, 400);
        statusMappingMap.put(MessageStatus.WRONG_PIN_FORMAT, 400);
        statusMappingMap.put(MessageStatus.ERROR_PIN_LENGTH, 400);
        statusMappingMap.put(MessageStatus.PIN_VERIFICATION_ERROR, 400);
        statusMappingMap.put(MessageStatus.PIN_ELEMENT_REQUIRED_FOR_THIS_TRANSACTION_TYPE, 400);

        statusMappingMap.put(MessageStatus.REJECTED, 400);
        statusMappingMap.put(MessageStatus.NOT_SUPPORTED_BY_RECEIVER, 400);
        statusMappingMap.put(MessageStatus.FUNCTION_NOT_AVAILABLE, 400);
        statusMappingMap.put(MessageStatus.FUNCTION_NOT_AVAILABLE_TO_USER, 400);
        statusMappingMap.put(MessageStatus.CARD_HOLDER_TRANSACTION_NOT_PERMITTED, 400);
        statusMappingMap.put(MessageStatus.TERMINAL_TRANSACTION_NOT_PERMITTED, 400);
        statusMappingMap.put(MessageStatus.SECURITY_VIOLATION, 400);
        statusMappingMap.put(MessageStatus.NO_SUFFICIENT_FUNDS, 400);
        statusMappingMap.put(MessageStatus.EXPIRED_CARD, 400);
        statusMappingMap.put(MessageStatus.PRIVATE_CARD, 400);
        statusMappingMap.put(MessageStatus.SUSPECTED_FRAUD, 400);

        // Conflict
        statusMappingMap.put(MessageStatus.CUSTMER_ACCOUNT_CONFLICT, 409);
        statusMappingMap.put(MessageStatus.DUPLICATE_BILL_PAYMENT_EXIST, 409);
        statusMappingMap.put(MessageStatus.DUPLICATE_RECORD_NEW_RECORD_REJECTED, 409);
        statusMappingMap.put(MessageStatus.DUPLICATE_RECORD_OLD_RECORD_REPLACED, 409);

        // Provider / Gateway
        statusMappingMap.put(MessageStatus.SC_ERROR_UNREACHABLE_PROVIDER, 502);
        statusMappingMap.put(MessageStatus.CONNECTION_NOT_ACCEPTED, 502);
        statusMappingMap.put(MessageStatus.CARD_ISSUER_OR_SWITCH_INOPERATIVE, 502);
        statusMappingMap.put(MessageStatus.TRANSACTION_RECEIVER_NOT_REFERENCED_FOR_SWITCH, 502);

        // Service unavailable
        statusMappingMap.put(MessageStatus.CARD_ISSUER_NOT_AVAILABLE, 503);
        statusMappingMap.put(MessageStatus.RECONCILIATION_PROCESS_NOT_AVAILABLE, 503);
        statusMappingMap.put(MessageStatus.CUT_OFF_IN_PROGRESS, 503);

        // Timeout
        statusMappingMap.put(MessageStatus.TIMEOUT, 504);

        // Internal Server Error
        statusMappingMap.put(MessageStatus.SC_ERROR_SYSTEM, 500);
        statusMappingMap.put(MessageStatus.SERVER_PROCESSING_ERROR, 500);
        statusMappingMap.put(MessageStatus.TRANSACTION_PROCESSING_ERROR, 500);
        statusMappingMap.put(MessageStatus.SYSTEM_DEFECT, 500);
        statusMappingMap.put(MessageStatus.CRYPTOGRAPHIC_ERROR, 500);
        statusMappingMap.put(MessageStatus.ERROR_VERIFYING_SIGNATORIES, 500);
        statusMappingMap.put(MessageStatus.CREATE_KARPARDAZ_FAILED, 500);
        statusMappingMap.put(MessageStatus.DELETE_KARPARDAZ_DELETE, 500);
        statusMappingMap.put(MessageStatus.FAILED_CREATE_KARPARDAZ, 500);
        statusMappingMap.put(MessageStatus.FAILED_CREATE_CUSTACC, 500);
        statusMappingMap.put(MessageStatus.FALED_DELETE_KARPARDAZ, 500);
        statusMappingMap.put(MessageStatus.FAILED_DELETE_CUSTACC, 500);
        statusMappingMap.put(MessageStatus.FILE_LOCKED, 500);
        statusMappingMap.put(MessageStatus.FILE_UNKNOWN, 500);
        statusMappingMap.put(MessageStatus.ZONE_CONTROL_ERROR, 500);
        statusMappingMap.put(MessageStatus.COUNTERS_NOT_AVAILABLE, 500);
        statusMappingMap.put(MessageStatus.UNSUCCESSFUL, 500);
        statusMappingMap.put(MessageStatus.FORMAT_ERROR, 500);
        statusMappingMap.put(MessageStatus.MANA_VALIDATION_ERROR, 500);
        statusMappingMap.put(MessageStatus.ETC, 500);
    }

    public static Integer toHttpStatus(MessageStatus status) {
        return statusMappingMap.get(status);
    }

}
