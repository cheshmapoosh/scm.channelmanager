package ir.daneshrefah.scm.uaa.service.otp.dto;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-02-17
 */
@SuperBuilder
@Getter
public class OtpSendResponse extends OtpBaseResponse {
    private final Otp otp;
}
