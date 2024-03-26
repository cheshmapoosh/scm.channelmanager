package ir.daneshrefah.scm.common.exception;

import static ir.daneshrefah.scm.common.model.error.ErrorCodes.ERROR_CODE_VALIDATION_PROVIDER_ASSET_NOT_FOUND;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-03-26
 */
public class NoAssetFoundException extends AbstractValidationException {

    public NoAssetFoundException() {
        super("asset" , "no asset found");
    }

    @Override
    public int getErrorCode() {
        return ERROR_CODE_VALIDATION_PROVIDER_ASSET_NOT_FOUND;
    }
}
