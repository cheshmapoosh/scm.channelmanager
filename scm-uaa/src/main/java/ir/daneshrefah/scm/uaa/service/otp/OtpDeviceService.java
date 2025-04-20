package ir.daneshrefah.scm.uaa.service.otp;

import ir.daneshrefah.scm.common.constant.AccessibleLocale;
import ir.daneshrefah.scm.common.data.entity.UserTokenDetails;
import ir.daneshrefah.scm.common.data.entity.person.GeneralPersonEntity;
import ir.daneshrefah.scm.common.data.entity.terminal.TerminalEntity;
import ir.daneshrefah.scm.common.data.repository.TerminalRepository;
import ir.daneshrefah.scm.common.data.repository.userTokenDetails.UserTokenDetailsRepository;
import ir.daneshrefah.scm.common.data.service.bundle.ResourceBundleService;
import ir.daneshrefah.scm.common.data.service.person.PersonService;
import ir.daneshrefah.scm.common.exception.InvalidInputException;
import ir.daneshrefah.scm.common.model.person.GeneralPerson;
import ir.daneshrefah.scm.common.model.user.AuthenticationMethod;
import ir.daneshrefah.scm.mq.jms.message.JakartaMessage;
import ir.daneshrefah.scm.uaa.common.model.user.User;
import ir.daneshrefah.scm.uaa.common.utils.AuthenticationUtils;
import ir.daneshrefah.scm.uaa.config.OtpDeviceProperties;
import ir.daneshrefah.scm.uaa.domain.otp.*;
import ir.daneshrefah.scm.uaa.exception.ImpossibleOTPException;
import ir.daneshrefah.scm.uaa.exception.RegisterNewUserException;
import ir.daneshrefah.scm.uaa.repository.authentication.UserEntity;
import ir.daneshrefah.scm.uaa.repository.authentication.UserRepository;
import ir.daneshrefah.scm.uaa.security.userDetails.UserCache;
import ir.daneshrefah.scm.uaa.service.otp.crypt.model.OtpChannel;
import ir.daneshrefah.scm.uaa.service.otp.dto.OtpRegisterDeviceRequest;
import ir.daneshrefah.scm.uaa.service.otp.dto.OtpVerifyRequest;
import ir.daneshrefah.scm.uaa.service.otp.dto.OtpVerifyResponse;
import ir.daneshrefah.scm.uaa.service.otp.generator.MessageGeneratorFactory;
import ir.daneshrefah.scm.uaa.service.otp.generator.OtpMessageGenerator;
import ir.daneshrefah.scm.uaa.service.user.XUserDetailService;
import ir.daneshrefah.scm.uaa.utils.RequestUtils;
import ir.daneshrefah.scm.utils.string.StringUtils;
import ir.daneshrefah.scm.utils.validation.ValidationUtils;
import jakarta.jms.Message;
import jakarta.jms.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.lang.IllegalStateException;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpDeviceService {
    public static final String MULE_CORRELATION_ID = "MULE_CORRELATION_ID";


    private final JmsTemplate avacasJmsTemplate;

    private final OtpDeviceProperties otpDeviceProperties;

    private final MessageGeneratorFactory requestGeneratorFactory;

    private final OtpChannel otpChannel;

    private final UserCache userCache;

    private final XUserDetailService xUserDetailService;

    private final TerminalRepository terminalRepository;

    private final UserRepository userRepository;

    private final PersonService personService;

    private final ResourceBundleService resourceBundleService;

    private final UserTokenDetailsRepository userTokenDetailsRepository;

    @Transactional
    public RegisterDeviceResponse registerOtpDevice(OtpRegisterDeviceRequest request, GeneralPersonEntity generalPersonEntity, UserEntity userEntity) {
        User loggedInUser = AuthenticationUtils.getLoggedInUser();
        assert loggedInUser != null;
        GeneralPerson loggedInUserPerson = loggedInUser.getPerson();
        GeneralPerson generalPerson = personService.findPersonByPersonId(loggedInUserPerson.getId());
        String employeeBranchCode = generalPerson.getBranchCode();
        ResponseMessageDetails responseBody = sendAndReceiveOTPRequest(userEntity, request.getOtpDeviceType().getValue(), generalPersonEntity.getUsername(), employeeBranchCode);
        validateAfterRegistration(responseBody);
        UserTokenDetails userTokenDetailEntity = createUserTokenDetailEntity(request, responseBody, userEntity);
        userTokenDetailsRepository.save(userTokenDetailEntity);
        String terminalCode = RequestUtils.extractRequestTerminalCode();
        updateUser(userEntity, terminalCode);
        return RegisterDeviceResponse.builder().success(true).resultCode(responseBody.getResultCode()).serialNo(responseBody.getMessageNO()).build();
    }

    public OtpVerifyResponse verifyOtp(OtpVerifyRequest request) {
        User user = request.getUser();
        GeneralPerson person = user.getPerson();
        boolean hasOTPAssignment = hasOTPAssignment(person.getId());
        if (!hasOTPAssignment) {
            String errorMessage = "Impossible OTP request for username: " + person.getUsername() + ", nickname: " + user.getNickname();
            log.error(errorMessage);
            throw new ImpossibleOTPException(errorMessage);
        }
        SecondPasswordAuthenticationToken authentication = new SecondPasswordAuthenticationToken(person.getUsername(), request.getClaimCode());
        ResponseMessageDetails responseBody;
        try {
            responseBody = sendAndReceiveOTPRequest(authentication, "", user.getNickname(), user.getCreatorBranch());
        } catch (Throwable ex) {
            log.error("Failed to send or receive OTP request for user: {} ,with exception: {} " , user.getNickname(), ex.getMessage());
            throw ex;
        }
        String resultCode = responseBody.getResultCode();
        if (Objects.equals(AvaCasResponseCode.OK.getCode(), resultCode)) {
            return OtpVerifyResponse.builder()
                    .otpType(request.getOtpType())
                    .isSuccessful(true)
                    .build();
        }
        log.info("OTP verification completed with result code [{}] for user with nickname '{}'", resultCode, user.getNickname());        Optional<String> errorMessage = resourceBundleService.get(AccessibleLocale.FA_IR.getLocale(), AvaCasResponseCode.getStatus(resultCode));//TODO read locale from request header
        return OtpVerifyResponse.builder().isSuccessful(false)
                .otpType(request.getOtpType())
                .errorMessage(errorMessage.orElse("Internal Error")).
                build();
    }

    private UserTokenDetails createUserTokenDetailEntity(OtpRegisterDeviceRequest request, ResponseMessageDetails responseBody, UserEntity userEntity) {
        User loggedInUser = AuthenticationUtils.getLoggedInUser();
        Long loggedInUserId = AuthenticationUtils.getLoggedInUserId();
        UserTokenDetails userTokenDetail = new UserTokenDetails();
        assert loggedInUser != null;
        String terminalCode = loggedInUser.getTerminalCode();
        TerminalEntity terminalEntity = terminalRepository.findByCode(terminalCode).orElseThrow(() -> new InvalidInputException("terminalCode"));
        userTokenDetail.setTerminal(terminalEntity);
        userTokenDetail.setActive(true);
        userTokenDetail.setAuthenticationMethod(AuthenticationMethod.OTP);
        userTokenDetail.setPerson(userEntity.getPerson());
        userTokenDetail.setTokenType(request.getOtpDeviceType().getValue());
        userTokenDetail.setAccessFromDate(new Date());
        userTokenDetail.setOtpSerialNo(((RegisterNewUserResponseBody) responseBody.getMessageBody()).getSerialNo());
        userTokenDetail.setActivationCode(((RegisterNewUserResponseBody) responseBody.getMessageBody()).getActivationCode());
        userTokenDetail.setCreatedBy(loggedInUserId);
        userTokenDetail.setCreationDate(new Date());
        return userTokenDetail;
    }

    private void validateAfterRegistration(ResponseMessageDetails responseBody) {
        if (!AvaCasResponseCode.OK.getCode().equals(responseBody.getResultCode())) {
            throw new RegisterNewUserException(responseBody.getResultCode());
        }
    }

    public boolean hasOTPAssignment(Long personId) {
        return userTokenDetailsRepository.countByPersonId(personId) > 0;
    }

    public void updateUser(UserEntity userEntity, String channelCode) {
        if (!StringUtils.isBlank(channelCode)) {
            updateUserCache(userEntity.getNickname(), channelCode);
            removeUserDetails(userEntity, channelCode);
        } else {
            List<UserEntity> userEntities = userRepository.findAllById(userEntity.getId());
            if (!userEntities.isEmpty()) {
                for (UserEntity entity : userEntities) {
                    updateUserCache(entity.getNickname());
                    removeUserDetails(entity.getNickname());
                }
            }
        }
    }

    private void removeUserDetails(UserEntity userEntity, String channelCode) {
        ValidationUtils.checkBlankString(userEntity.getNickname(), () -> new InvalidInputException("username"));
        ValidationUtils.checkBlankString(channelCode, () -> new InvalidInputException("channelCode"));
        xUserDetailService.removeXUserByUsernameAndChannelCode(userEntity, channelCode);
    }

    private void removeUserDetails(String username) {
        ValidationUtils.checkBlankString(username, () -> new InvalidInputException("username"));
        xUserDetailService.removeXUserByUsername(username);
    }

    private void updateUserCache(String username, String channelCode) {
        userCache.removeUserFromCache(username, channelCode);
    }

    private void updateUserCache(String username) {
        userCache.removeUserFromCache(username);
    }

    public ResponseMessageDetails sendAndReceiveOTPRequest(Object request, String otpTokenType, String username, String employeeBranchCode) {
        OtpMessageGenerator messageGenerator = requestGeneratorFactory.getMessageGenerator(request);
        String messageNo = String.valueOf(new Random().nextInt(400000000));
        OtpMessageModel otpMessage = new OtpMessageModel(request, messageNo, otpChannel.getId(), otpTokenType, employeeBranchCode);
        OtpMessage requestBeforeTransformation = messageGenerator.generateRequest(otpMessage);
        byte[] requestMessage = messageGenerator.transformRequest(requestBeforeTransformation, otpChannel);
        String correlationId = UUID.randomUUID().toString();
        send(requestMessage, correlationId);
        Object received = receive(correlationId);
        return (ResponseMessageDetails) messageGenerator.generateResponse((byte[]) received, otpChannel);
    }

    public void send(final Serializable sendObject, final String correlationId) {
        avacasJmsTemplate.send(otpDeviceProperties.getSendQueueName(), session -> {
            BytesMessage message = session.createBytesMessage();
            message.writeBytes((byte[]) sendObject);
            message.setStringProperty(MULE_CORRELATION_ID, correlationId);
            message.setJMSCorrelationID(correlationId);
            return message;
        });
    }

    private Object receive(String correlationId) {
        String selector = "JMSCorrelationID ='" + correlationId + "'";
        String receiveQueueName = otpDeviceProperties.getReceiverQueueName();
        avacasJmsTemplate.setReceiveTimeout(20000);
        Message receivedMessage = avacasJmsTemplate.receiveSelected(receiveQueueName, selector);
        if (receivedMessage instanceof JakartaMessage jakartaMessage) {
            com.ibm.jms.JMSBytesMessage jakartareceivedMessage = jakartaMessage.getJakartaMessage();
            if (jakartareceivedMessage instanceof TextMessage) {
                try {
                    return ((TextMessage) jakartareceivedMessage).getText();
                } catch (JMSException e) {
                    throw new RuntimeException("JMSException occurred");
                }
            } else if (jakartareceivedMessage != null) {
                try {
                    byte[] receivedData = new byte[128];
                    jakartareceivedMessage.readBytes(receivedData);
                    return receivedData;
                } catch (javax.jms.JMSException e) {
                    throw new RuntimeException("JMSException occurred");
                }
            } else {
                throw new IllegalStateException("Expected " + ObjectMessage.class + "but received " + receivedMessage);
            }
        } else {
            throw new RuntimeException("Exceeded max receive timeout: " +
                    avacasJmsTemplate.getReceiveTimeout());
        }
    }
}
