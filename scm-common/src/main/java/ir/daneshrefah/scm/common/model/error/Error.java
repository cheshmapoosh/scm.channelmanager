package ir.daneshrefah.scm.common.model.error;

import lombok.Getter;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-23
 */
@Getter
public class Error {

    /**
     * It contains 'propertyName' that has error in 'VALIDATION' type
     * */
    private final String source;
    private final String errorCode;
    private final String message;

    public Error(String source, Integer errorCode, String message) {
        this(source, "SCM-" + errorCode, message);
    }

    public Error(String source, String errorCode, String message) {
        this.source = source;
        this.errorCode = errorCode;
        this.message = message;
    }
}
