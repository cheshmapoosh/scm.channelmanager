package ir.daneshrefah.scm.observation.attributes;

import ir.daneshrefah.scm.observation.ObservationAttributeKey;
import ir.daneshrefah.scm.observation.ObservationAttributePresence;

public final class ScmLegacyAttributes {
    private ScmLegacyAttributes() {
    }

    public static final ObservationAttributeKey<String> SOURCE_TABLE = ObservationAttributeKey.stringKey("scm.legacy.source_table", ObservationAttributePresence.EVENT_OPTIONAL, "Legacy source table");
    public static final ObservationAttributeKey<String> TRANSACTION_LOG_ID = ObservationAttributeKey.stringKey("scm.legacy.transaction_log_id", ObservationAttributePresence.EVENT_OPTIONAL, "Legacy transaction log ID");
    public static final ObservationAttributeKey<String> MESSAGE_LOG_ID = ObservationAttributeKey.stringKey("scm.legacy.message_log_id", ObservationAttributePresence.EVENT_OPTIONAL, "Legacy message log ID");
    public static final ObservationAttributeKey<String> ARCHIVE_NO = ObservationAttributeKey.stringKey("scm.legacy.archive_no", ObservationAttributePresence.EVENT_OPTIONAL, "Legacy archive number");
    public static final ObservationAttributeKey<String> EB_SERVICE_ID = ObservationAttributeKey.stringKey("scm.legacy.eb_service_id", ObservationAttributePresence.EVENT_OPTIONAL, "Legacy EB service ID");
    public static final ObservationAttributeKey<String> TRANSACTION_STATE_ID = ObservationAttributeKey.stringKey("scm.legacy.transaction_state_id", ObservationAttributePresence.EVENT_OPTIONAL, "Legacy transaction state ID");
    public static final ObservationAttributeKey<String> EXTERNAL_SEQUENCE_ID = ObservationAttributeKey.stringKey("scm.external_sequence_id", ObservationAttributePresence.EVENT_OPTIONAL, "External sequence ID");
    public static final ObservationAttributeKey<String> ORIGINAL_SEQUENCE_ID = ObservationAttributeKey.stringKey("scm.original_sequence_id", ObservationAttributePresence.EVENT_OPTIONAL, "Original sequence ID");
    public static final ObservationAttributeKey<String> DOCUMENT_NO = ObservationAttributeKey.stringKey("scm.document.no", ObservationAttributePresence.EVENT_OPTIONAL, "Document number");
    public static final ObservationAttributeKey<String> TERMINAL_ID = ObservationAttributeKey.stringKey("scm.terminal.id", ObservationAttributePresence.EVENT_OPTIONAL, "Terminal ID");
    public static final ObservationAttributeKey<String> TERMINAL_TYPE = ObservationAttributeKey.stringKey("scm.terminal.type", ObservationAttributePresence.EVENT_OPTIONAL, "Terminal type");
    public static final ObservationAttributeKey<Boolean> TRANSACTION_INTER_BANK = ObservationAttributeKey.booleanKey("scm.transaction.inter_bank", ObservationAttributePresence.EVENT_OPTIONAL, "Whether transaction is inter-bank");
    public static final ObservationAttributeKey<Boolean> TRANSACTION_DUPLICATE = ObservationAttributeKey.booleanKey("scm.transaction.duplicate", ObservationAttributePresence.EVENT_OPTIONAL, "Whether transaction is duplicate");
    public static final ObservationAttributeKey<String> MESSAGE_SEQUENCE_ID = ObservationAttributeKey.stringKey("scm.message.sequence_id", ObservationAttributePresence.EVENT_OPTIONAL, "Message sequence ID");
    public static final ObservationAttributeKey<String> STATUS_CODE = ObservationAttributeKey.stringKey("scm.status.code", ObservationAttributePresence.EVENT_OPTIONAL, "Status code");
    public static final ObservationAttributeKey<String> TRANSACTION_STATUS = ObservationAttributeKey.stringKey("scm.transaction.status", ObservationAttributePresence.EVENT_OPTIONAL, "Transaction status");
    public static final ObservationAttributeKey<String> TRANSACTION_TYPE = ObservationAttributeKey.stringKey("scm.transaction.type", ObservationAttributePresence.EVENT_OPTIONAL, "Transaction type");
    public static final ObservationAttributeKey<String> CSP_USERNAME = ObservationAttributeKey.stringKey("scm.csp.username", ObservationAttributePresence.EVENT_OPTIONAL, "CSP username");
    public static final ObservationAttributeKey<Double> AMOUNT = ObservationAttributeKey.doubleKey("scm.amount", ObservationAttributePresence.EVENT_OPTIONAL, "Amount");
    public static final ObservationAttributeKey<String> DESTINATION = ObservationAttributeKey.stringKey("scm.destination", ObservationAttributePresence.EVENT_OPTIONAL, "Destination account, card, IBAN, or business destination");
}
