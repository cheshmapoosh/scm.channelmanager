package ir.daneshrefah.scm.common.exception;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-29
 */
public class TerminalServiceNotFoundException extends BaseException {

    private final String terminalCode;
    private final String serviceCode;
    public TerminalServiceNotFoundException(String terminalCode, String serviceCode) {
        super("service by code '" + serviceCode + "' with terminal code '" + terminalCode + "' not found.", null);
        this.terminalCode = terminalCode;
        this.serviceCode = serviceCode;
    }

    @Override
    public String getSource() {
        return serviceCode;
    }
}
