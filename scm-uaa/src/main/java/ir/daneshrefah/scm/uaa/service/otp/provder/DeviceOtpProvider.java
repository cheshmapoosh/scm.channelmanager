package ir.daneshrefah.scm.uaa.service.otp.provder;

import ir.daneshrefah.scm.uaa.domain.otp.OtpType;
import ir.daneshrefah.scm.uaa.service.otp.dto.OtpVerifyRequest;
import org.springframework.stereotype.Component;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-30
 */
@Component
public class DeviceOtpProvider implements OtpProvider {

    @Override
    public boolean verifyOtp(OtpVerifyRequest request) {
        return false;
    }

    @Override
    public OtpType getType() {
        return OtpType.DEVICE;
    }

}
