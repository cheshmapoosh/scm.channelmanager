package ir.daneshrefah.scm.common.exception;

import ir.daneshrefah.scm.common.model.message.MessageStatus;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-02-24
 *
 * @see ir.daneshrefah.scm.common.error.spec.AbstractBaseException
 * @see ir.daneshrefah.scm.common.error.spec.ExceptionSourceAware
 */
@Deprecated
public interface ErrorCodeAwareException {

    int getErrorCode();

    String getMessage();

    String getSource();

    MessageStatus getStatus();

}
