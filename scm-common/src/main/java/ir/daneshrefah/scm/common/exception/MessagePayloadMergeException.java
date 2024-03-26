package ir.daneshrefah.scm.common.exception;

import com.fasterxml.jackson.databind.JsonNode;
import ir.daneshrefah.scm.common.model.error.ErrorCodes;
import ir.daneshrefah.scm.common.model.message.MessageStatus;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-03-26
 */
public class MessagePayloadMergeException extends BaseException implements ErrorCodeAwareException {

    private final JsonNode payload;

    public MessagePayloadMergeException(String message, JsonNode payload, Exception cause) {
        super(message, cause);
        this.payload = payload;
    }

    @Override
    public int getErrorCode() {
        return ErrorCodes.ERROR_CODE_PAYLOAD_MERGE_ERROR;
    }

    @Override
    public MessageStatus getStatus() {
        return MessageStatus.SC_ERROR_SYSTEM;
    }

    @Override
    public String getSource() {
        return null != payload ? payload.toString() : "null";
    }
}
