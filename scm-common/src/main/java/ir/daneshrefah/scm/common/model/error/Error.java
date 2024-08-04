package ir.daneshrefah.scm.common.model.error;

import com.fasterxml.jackson.annotation.JsonIgnore;
import ir.daneshrefah.scm.common.model.message.MessageStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-23
 */
@Getter
@NoArgsConstructor
public class Error  {

    /**
     * It contains 'propertyName' that has error in 'VALIDATION' type
     * */
    private String source;
    private String errorCode;
    private String message;
    @JsonIgnore
    private Exception exception;
    private MessageStatus status;


    public Error(String source, Integer errorCode, String message) {
        this(source, "SCM-" + errorCode, message,null, null);
    }

    public Error(String source, Integer errorCode, String message, Exception exception) {
        this(source, "SCM-" + errorCode,message,null, exception);
    }

    public Error(String source, Integer errorCode, String message,MessageStatus status, Exception exception) {
        this(source, "SCM-" + errorCode, message,status, exception);
    }

    public Error(String source, String errorCode, String message,Exception exception) {
        this.source = source;
        this.errorCode = errorCode;
        this.message = message;
        this.exception = exception;
        this.status = null;
    }

    public Error(String source, String errorCode, String message, MessageStatus status,Exception exception) {
        this.source = source;
        this.errorCode = errorCode;
        this.message = message;
        this.exception = exception;
        this.status = status;
    }


}
