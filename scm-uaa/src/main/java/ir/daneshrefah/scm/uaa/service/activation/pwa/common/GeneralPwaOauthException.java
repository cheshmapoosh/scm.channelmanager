package ir.daneshrefah.scm.uaa.service.activation.pwa.common;

import ir.daneshrefah.scm.uaa.common.constants.PwaOauthMessage;
import lombok.Getter;

@Getter
public class GeneralPwaOauthException extends RuntimeException {

    private final PwaOauthMessage exceptionMessage;

    public GeneralPwaOauthException(PwaOauthMessage exceptionMessage) {
        super(exceptionMessage.name());
        this.exceptionMessage = exceptionMessage;
    }
}
