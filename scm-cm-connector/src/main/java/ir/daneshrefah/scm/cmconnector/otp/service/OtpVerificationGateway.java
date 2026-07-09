package ir.daneshrefah.scm.cmconnector.otp.service;

import ir.daneshrefah.scm.cmconnector.otp.model.CmOtpVerifyRequest;
import ir.daneshrefah.scm.cmconnector.otp.model.CmOtpVerifyResponse;
import ir.daneshrefah.scm.uaa.starter.security.ScmPrincipal;

public interface OtpVerificationGateway {

    CmOtpVerifyResponse verify(
            ScmPrincipal principal,
            CmOtpVerifyRequest request
    );
}
