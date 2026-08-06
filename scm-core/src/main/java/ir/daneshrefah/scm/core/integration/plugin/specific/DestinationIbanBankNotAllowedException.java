package ir.daneshrefah.scm.core.integration.plugin.specific;

import lombok.Getter;

@Getter
public class DestinationIbanBankNotAllowedException extends RuntimeException {

    private final String errorCode;

    public DestinationIbanBankNotAllowedException(String errorCode) {
        super(errorCode);
        this.errorCode = errorCode;
    }
}
