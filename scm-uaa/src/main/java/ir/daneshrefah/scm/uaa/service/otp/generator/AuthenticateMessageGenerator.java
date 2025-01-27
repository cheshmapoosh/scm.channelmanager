package ir.daneshrefah.scm.uaa.service.otp.generator;

import ir.daneshrefah.scm.uaa.domain.otp.*;

public class AuthenticateMessageGenerator extends AbstractOtpMessageGenerator {

    private static final String PIN = "2";
    private static final String REQUEST_TYPE = "2";

    public String getMessageType() {
        return REQUEST_TYPE;
    }

    public Message createRequestBody(OtpMessageModel request) {
        return new AuthenticationRequestBody((SecondPasswordAuthenticationToken) request.getRequest(), PIN, request.getOtpChannelId());
    }

    Message createResponseBody() {
        return new AuthenticationResponseBody();
    }
}
