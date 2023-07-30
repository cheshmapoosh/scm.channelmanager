package ir.daneshrefah.scm.plugin.api.exception;

import ir.daneshrefah.scm.plugin.api.constants.ErrorCodes;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-29
 */
public abstract class RateLimitException extends BaseException {

    public RateLimitException(String correlationId) {
        super(correlationId, "SCM", ErrorCodes.ERROR_RATE_LIMIT, "");
    }

}
