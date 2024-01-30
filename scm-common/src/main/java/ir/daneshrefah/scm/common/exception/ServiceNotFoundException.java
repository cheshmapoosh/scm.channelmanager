package ir.daneshrefah.scm.common.exception;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-29
 */
public class ServiceNotFoundException extends BaseException {

    private final String serviceCode;

    public ServiceNotFoundException(String serviceCode) {
        super("service by code '" + (null != serviceCode ? serviceCode : "null") + "' not found.", null);
        this.serviceCode = serviceCode;
    }

    @Override
    public String getSource() {
        return serviceCode;
    }
}
