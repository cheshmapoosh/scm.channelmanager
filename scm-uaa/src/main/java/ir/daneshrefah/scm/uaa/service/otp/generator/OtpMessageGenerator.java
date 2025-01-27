package ir.daneshrefah.scm.uaa.service.otp.generator;

import ir.daneshrefah.scm.uaa.domain.otp.*;
import ir.daneshrefah.scm.uaa.service.otp.crypt.model.OtpChannel;

public interface OtpMessageGenerator {

    OtpMessage generateRequest(OtpMessageModel otmm);

    Object generateResponse(byte[] cipherResponse, OtpChannel otpChannel);

    byte[] transformRequest(OtpMessage message, OtpChannel otpChannel);
}
