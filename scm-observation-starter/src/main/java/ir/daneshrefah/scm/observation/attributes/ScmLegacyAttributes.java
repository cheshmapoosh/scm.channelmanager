package ir.daneshrefah.scm.observation.attributes;

import ir.daneshrefah.scm.observation.ObservationAttributeKey;

public final class ScmLegacyAttributes {
    private ScmLegacyAttributes() {
    }

    public static final ObservationAttributeKey<String> SOURCE_TABLE = ObservationAttributeKey.stringKey("scm.legacy.source_table", "Legacy source table");
    public static final ObservationAttributeKey<String> TRANSACTION_LOG_ID = ObservationAttributeKey.stringKey("scm.legacy.transaction_log_id", "Legacy transaction log ID");
    public static final ObservationAttributeKey<String> MESSAGE_LOG_ID = ObservationAttributeKey.stringKey("scm.legacy.message_log_id", "Legacy message log ID");
    public static final ObservationAttributeKey<String> ARCHIVE_NO = ObservationAttributeKey.stringKey("scm.legacy.archive_no", "Legacy archive number");
    public static final ObservationAttributeKey<String> EB_SERVICE_ID = ObservationAttributeKey.stringKey("scm.legacy.eb_service_id", "Legacy EB service ID");
    public static final ObservationAttributeKey<String> TRANSACTION_STATE_ID = ObservationAttributeKey.stringKey("scm.legacy.transaction_state_id", "Legacy transaction state ID");
    public static final ObservationAttributeKey<String> EXTERNAL_SEQUENCE_ID = ObservationAttributeKey.stringKey("scm.external_sequence_id", "External sequence ID");
    public static final ObservationAttributeKey<String> ORIGINAL_SEQUENCE_ID = ObservationAttributeKey.stringKey("scm.original_sequence_id", "Original sequence ID");
    public static final ObservationAttributeKey<String> DOCUMENT_NO = ObservationAttributeKey.stringKey("scm.document.no", "Document number");
    public static final ObservationAttributeKey<String> TERMINAL_ID = ObservationAttributeKey.stringKey("scm.terminal.id", "Terminal ID");
    public static final ObservationAttributeKey<String> TERMINAL_TYPE = ObservationAttributeKey.stringKey("scm.terminal.type", "Terminal type");
    public static final ObservationAttributeKey<Boolean> TRANSACTION_INTER_BANK = ObservationAttributeKey.booleanKey("scm.transaction.inter_bank", "Whether transaction is inter-bank");
    public static final ObservationAttributeKey<Boolean> TRANSACTION_DUPLICATE = ObservationAttributeKey.booleanKey("scm.transaction.duplicate", "Whether transaction is duplicate");
    public static final ObservationAttributeKey<String> MESSAGE_SEQUENCE_ID = ObservationAttributeKey.stringKey("scm.message.sequence_id", "Message sequence ID");
    public static final ObservationAttributeKey<String> STATUS_CODE = ObservationAttributeKey.stringKey("scm.status.code", "Status code");
    public static final ObservationAttributeKey<String> TRANSACTION_STATUS = ObservationAttributeKey.stringKey("scm.transaction.status", "Transaction status");
    public static final ObservationAttributeKey<String> TRANSACTION_TYPE = ObservationAttributeKey.stringKey("scm.transaction.type", "Transaction type");
    public static final ObservationAttributeKey<String> CSP_USERNAME = ObservationAttributeKey.stringKey("scm.csp.username", "CSP username");
    public static final ObservationAttributeKey<Double> AMOUNT = ObservationAttributeKey.doubleKey("scm.amount", "Amount");
    public static final ObservationAttributeKey<String> DESTINATION = ObservationAttributeKey.stringKey("scm.destination", "Destination account, card, IBAN, or business destination");
}
