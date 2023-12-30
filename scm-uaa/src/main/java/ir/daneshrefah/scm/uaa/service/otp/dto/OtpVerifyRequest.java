package ir.daneshrefah.scm.uaa.service.otp.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-30
 */
@Getter
@Setter
public class OtpVerifyRequest extends OtpBaseRequest {

    String code;

}
