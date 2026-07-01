package ir.daneshrefah.scm.common.model.error;

import com.fasterxml.jackson.annotation.JsonIgnore;
import ir.daneshrefah.scm.common.model.message.MessageStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-23
 */
@Getter
@NoArgsConstructor
@Accessors(chain = true)
public class Error {

    /**
     * It contains 'propertyName' that has error in 'VALIDATION' type
     */
    private String source;
    private String errorCode;
    private String message;
    private String messageFa;
    @JsonIgnore
    private Exception exception;
    private MessageStatus status;


    public Error(String source, Integer errorCode, String message) {
        this(source, "SCM-" + errorCode, message, null, null, null);
    }

    public Error(String source, Integer errorCode, String message, Exception exception) {
        this(source, "SCM-" + errorCode, message, null, null, exception);
    }

    public Error(String source, Integer errorCode, String message, MessageStatus status, Exception exception) {
        this(source, "SCM-" + errorCode, message, null, status, exception);
    }

    public Error(String source, Integer errorCode, String message, String messageFa, MessageStatus status, Exception exception) {
        this(source, "SCM-" + errorCode, message, messageFa, status, exception);
    }

    public Error(String source, String errorCode, String message, Exception exception) {
        this.source = source;
        this.errorCode = errorCode;
        this.message = message;
        this.exception = exception;
        this.status = null;
    }

    public Error(String source, String errorCode, String message, String messageFa, MessageStatus status, Exception exception) {
        this.source = source;
        this.errorCode = errorCode;
        this.message = message;
        this.messageFa = messageFa;
        this.exception = exception;
        this.status = status;
    }


}
