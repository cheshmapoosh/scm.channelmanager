package ir.daneshrefah.scm.common.exception;

import ir.daneshrefah.scm.common.error.spec.AbstractBaseException;
import ir.daneshrefah.scm.common.error.spec.ExceptionSourceAware;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-02-13
 */
public abstract class BasePersonException extends AbstractBaseException  {

    public BasePersonException(String message) {
        super(message, null);
    }

}
