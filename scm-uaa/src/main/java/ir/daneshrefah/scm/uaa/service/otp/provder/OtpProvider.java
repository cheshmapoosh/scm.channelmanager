package ir.daneshrefah.scm.uaa.service.otp.provder;

import ir.daneshrefah.scm.uaa.domain.otp.OtpType;
import ir.daneshrefah.scm.uaa.service.otp.dto.OtpVerifyRequest;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-30
 */
public interface OtpProvider {

    public boolean verifyOtp(OtpVerifyRequest request);

    public OtpType getType();

}
