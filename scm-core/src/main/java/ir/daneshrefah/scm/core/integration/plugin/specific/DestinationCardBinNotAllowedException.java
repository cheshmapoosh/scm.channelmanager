package ir.daneshrefah.scm.core.integration.plugin.specific;

import lombok.Getter;

@Getter
public class DestinationCardBinNotAllowedException extends RuntimeException {

    private final String errorCode;

    public DestinationCardBinNotAllowedException(String errorCode) {
        super(errorCode);
        this.errorCode = errorCode;
    }
}
