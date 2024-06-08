package ir.daneshrefah.scm.common.exception;

import com.fasterxml.jackson.databind.JsonNode;
import ir.daneshrefah.scm.common.error.ExceptionInformation;
import ir.daneshrefah.scm.common.error.ExceptionInformationBuilder;
import ir.daneshrefah.scm.common.error.spec.AbstractBaseException;
import ir.daneshrefah.scm.common.error.spec.ExceptionSourceAware;
import ir.daneshrefah.scm.common.model.error.ErrorCodes;
import ir.daneshrefah.scm.common.model.message.MessageStatus;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-03-26
 */
public class MessagePayloadMergeException extends AbstractBaseException implements ExceptionSourceAware {

    private final JsonNode payload;

    public MessagePayloadMergeException(JsonNode payload, Exception cause) {
        super("incompatible array type of payloads", cause);
        this.payload = payload;
    }

    @Override
    public String getSource() {
        return null != payload ? payload.toString() : "null";
    }

    @Override
    public ExceptionInformation getExceptionInformation() {
        return ExceptionInformationBuilder
                .createInstance().buildWithStatus(MessageStatus.SC_ERROR_SYSTEM);
    }
}
