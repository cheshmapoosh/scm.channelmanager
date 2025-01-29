package ir.daneshrefah.scm.uaa.service.otp.dto;

import ir.daneshrefah.scm.uaa.repository.authentication.UserEntity;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-30
 */
@SuperBuilder
@Getter
public class OtpVerifyRequest extends OtpBaseRequest {
    private String claimCode;
    private UserEntity userEntity;
}
