package ir.daneshrefah.scm.uaa.exception;

import ir.daneshrefah.scm.common.exception.BaseException;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-02-13
 */
public abstract class BaseCIFException extends BaseException {

    public BaseCIFException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public String getSource() {
        return "CIF";
    }
}
