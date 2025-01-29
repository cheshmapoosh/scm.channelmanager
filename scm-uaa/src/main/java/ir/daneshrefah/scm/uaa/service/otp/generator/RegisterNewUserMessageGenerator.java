package ir.daneshrefah.scm.uaa.service.otp.generator;

import ir.daneshrefah.scm.uaa.domain.otp.Message;
import ir.daneshrefah.scm.uaa.domain.otp.OtpMessageModel;
import ir.daneshrefah.scm.uaa.domain.otp.RegisterNewUserRequestBody;
import ir.daneshrefah.scm.uaa.domain.otp.RegisterNewUserResponseBody;
import ir.daneshrefah.scm.uaa.repository.authentication.UserEntity;
import org.springframework.util.Assert;

public class RegisterNewUserMessageGenerator extends AbstractOtpMessageGenerator {

    private static final String REQUEST_TYPE = "3";
    private static final String AUTHENTICATION_MODE = "1";

    public String getMessageType() {
        return REQUEST_TYPE;
    }

    public Message createRequestBody(OtpMessageModel otpMessage) {
        Assert.isInstanceOf(UserEntity.class, otpMessage.getRequest());
        if (otpMessage.getRequest() != null) {
            return new RegisterNewUserRequestBody(
                    (UserEntity) otpMessage.getRequest(),
                    AUTHENTICATION_MODE,
                    otpMessage.getOtpChannelId(),
                    otpMessage.getTokenType(),
                    otpMessage.getBranchCode());
        }
        return null;
    }

    Message createResponseBody() {
        return new RegisterNewUserResponseBody();
    }
}
