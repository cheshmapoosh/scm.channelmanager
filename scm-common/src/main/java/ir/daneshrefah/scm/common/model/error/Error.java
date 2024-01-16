package ir.daneshrefah.scm.common.model.error;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-23
 */
@AllArgsConstructor
@Getter
public class Error {

    /**
     * It contains scm error code that list exist in {@link ErrorType}
     * */
    private ErrorType type;
    /**
     * It contains 'propertyName' that has error in 'VALIDATION' type
     * */
    private String provider;
    /**
     * It contains 'propertyName' that has error in 'VALIDATION' type
     * */
    private String source;
    private String errorCode;
    private String message;

}
