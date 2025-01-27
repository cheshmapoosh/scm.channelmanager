package ir.daneshrefah.scm.uaa.service.otp.generator;

import ir.daneshrefah.scm.uaa.repository.authentication.UserEntity;
import org.springframework.stereotype.Component;

@Component
public class MessageGeneratorFactory {

    public OtpMessageGenerator getMessageGenerator(Object request) {
        return request instanceof UserEntity ?
                new RegisterNewUserMessageGenerator() :
                new AuthenticateMessageGenerator();
    }
}