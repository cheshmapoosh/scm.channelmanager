package ir.daneshrefah.scm.common.error.spec;


import ir.daneshrefah.scm.common.error.ExceptionInformation;
import ir.daneshrefah.scm.common.error.ExceptionInformationBuilder;

import static ir.daneshrefah.scm.common.constant.BundleDefaults.EXCEPTION_BUNDLE_DEFAULT_PREFIX;

/**
 * @version 1.0.0
 * @author abdolahi.d
 */
public abstract class AbstractBaseException extends RuntimeException{

    private final String technicalMessage;

    public AbstractBaseException(String technicalMessage, Throwable cause) {
        super(technicalMessage, cause);
        this.technicalMessage = technicalMessage;
    }

    @Override
    public String getMessage() {
        return this.technicalMessage;
    }

    /**
     * @apiNote default locale : <em>en-US</em>
     * @see ExceptionInformationBuilder
     */
    public abstract ExceptionInformation getExceptionInformation();

    public String getBundleKey(){
        return EXCEPTION_BUNDLE_DEFAULT_PREFIX + this.getClass().getName();
    }
}
