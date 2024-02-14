package ir.daneshrefah.scm.common.exception;

import lombok.Getter;

import static ir.daneshrefah.scm.common.model.error.ErrorCodes.ERROR_CODE_NO_RECORD_FOUND;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-28
 */
@Getter
public class NoMatchRecordFoundException extends AbstractValidationException {

    private final String providerCode;

    public NoMatchRecordFoundException(String source) {
        this(source, null);
    }

    public NoMatchRecordFoundException(String providerCode, String source) {
        super(source, "no " + (null != providerCode ? providerCode : "local") + "[" + source + "] found.");
        this.providerCode = providerCode;
    }

    @Override
    public int getErrorCode() {
        return ERROR_CODE_NO_RECORD_FOUND;
    }

    /*private String source;
    public NoMatchRecordFoundException(String source) {
        this(source, null);
    }

    public NoMatchRecordFoundException(String source, Throwable cause) {
        super("no " + source + " found.", cause);
        this.source = source;
    }*/

}
