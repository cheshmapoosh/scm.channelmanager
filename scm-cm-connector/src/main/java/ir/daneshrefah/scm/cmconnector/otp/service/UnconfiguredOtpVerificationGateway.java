package ir.daneshrefah.scm.cmconnector.otp.service;

import ir.daneshrefah.scm.cmconnector.otp.model.CmOtpVerifyRequest;
import ir.daneshrefah.scm.cmconnector.otp.model.CmOtpVerifyResponse;
import ir.daneshrefah.scm.uaa.client.security.ScmPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class UnconfiguredOtpVerificationGateway implements OtpVerificationGateway {

    @Override
    public CmOtpVerifyResponse verify(
            ScmPrincipal principal,
            CmOtpVerifyRequest request
    ) {
        throw new ResponseStatusException(
                HttpStatus.NOT_IMPLEMENTED,
                "OTP verification gateway is not configured"
        );
    }
}
