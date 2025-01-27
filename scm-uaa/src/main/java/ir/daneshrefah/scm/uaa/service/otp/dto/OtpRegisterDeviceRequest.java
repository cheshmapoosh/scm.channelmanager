package ir.daneshrefah.scm.uaa.service.otp.dto;

import ir.daneshrefah.scm.common.constant.otp.OtpDeviceType;
import lombok.Data;

@Data
public class OtpRegisterDeviceRequest {

    private String nationalCode;
    private OtpDeviceType otpDeviceType;
    private String otpSerialNo;
}
