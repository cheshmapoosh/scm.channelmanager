package ir.daneshrefah.scm.uaa.domain.otp;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OtpMessageModel {

    private Object request;
    private String requestNo;
    private String otpChannelId;
    private String tokenType;
    private String branchCode;

    public OtpMessageModel(Object request, String requestNo, String otpChannelId, String tokenType, String branchCode) {
        this.request = request;
        this.requestNo = requestNo;
        this.otpChannelId = otpChannelId;
        this.tokenType = tokenType;
        this.branchCode = branchCode;
    }
}
