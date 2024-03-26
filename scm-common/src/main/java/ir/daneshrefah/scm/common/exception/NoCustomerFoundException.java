package ir.daneshrefah.scm.common.exception;

import static ir.daneshrefah.scm.common.model.error.ErrorCodes.ERROR_CODE_VALIDATION_PROVIDER_CUSTOMER_NOT_FOUND;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-03-26
 */
public class NoCustomerFoundException extends AbstractValidationException {

    public NoCustomerFoundException() {
        super("customer" , "no customer found");
    }

    @Override
    public int getErrorCode() {
        return ERROR_CODE_VALIDATION_PROVIDER_CUSTOMER_NOT_FOUND;
    }
}
