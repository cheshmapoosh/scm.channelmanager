package ir.daneshrefah.scm.uaa.service.activation.pwa.services.activation;

import ir.daneshrefah.scm.common.constant.TerminalType;
import ir.daneshrefah.scm.common.model.person.UserStatus;
import ir.daneshrefah.scm.uaa.common.constants.AuthStatus;
import ir.daneshrefah.scm.uaa.common.constants.PwaOauthMessage;
import ir.daneshrefah.scm.uaa.common.model.user.User;
import ir.daneshrefah.scm.uaa.domain.pwa.Register;
import ir.daneshrefah.scm.uaa.domain.pwa.UserActivation;
import ir.daneshrefah.scm.uaa.mapper.UserMapper;
import ir.daneshrefah.scm.uaa.repository.authentication.UserRepository;
import ir.daneshrefah.scm.uaa.security.authentication.token.PreAuthenticationToken;
import ir.daneshrefah.scm.uaa.service.activation.pwa.common.GeneralPwaOauthException;
import ir.daneshrefah.scm.uaa.service.activation.pwa.common.PwaOauthResponseMapper;
import ir.daneshrefah.scm.uaa.service.activation.pwa.model.ActivationRequest;
import ir.daneshrefah.scm.uaa.service.activation.pwa.model.ActivationRequestHeader;
import ir.daneshrefah.scm.uaa.service.activation.pwa.model.ActivationResponse;
import ir.daneshrefah.scm.uaa.service.client.ClientVersionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static ir.daneshrefah.scm.uaa.common.constants.PwaOauthMessage.CLIENT_NOT_FOUND;

@Service
@ConditionalOnBean(name = "activationDataSource")
@RequiredArgsConstructor
@Slf4j
public class PwaActivationServiceImpl implements PwaActivationService {

    private final ClientVersionService clientVersionService;
    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final PwaUserRegisterService pwaUserRegisterService;
    private final UserActivationService activationService;
    private final PwaOauthResponseMapper responseMapper;
    private final PwaNotificationCenter pwaNotificationCenter;


    @Override
    @Transactional(transactionManager = "activationTransactionManager")
    public ActivationResponse verificationRequest(ActivationRequest request) {
        findUser(request).orElseThrow(()-> new GeneralPwaOauthException(CLIENT_NOT_FOUND));
        UserActivation userActivation = activationService.findLastActivationRequest(request);
        boolean isValid = activationService.validateActivationCode(request,userActivation);
        userActivation.setCodeValid(isValid);
        if (isValid) {
            userActivation.setActivated(true);
            activationService.updateActivationStatus(userActivation);
            pwaUserRegisterService.saveRegistry(request,AuthStatus.SUCCEEDED);
            ActivationResponse response = responseMapper.getMessage(PwaOauthMessage.CLIENT_SUCCESSFULLY_ACTIVATED);
            response.setData(userActivation.getRegistryToken());
            return response;
        }else {
            pwaUserRegisterService.saveRegistry(request,AuthStatus.FAILED);
            return responseMapper.getMessage(PwaOauthMessage.CLIENT_INVALID_OTP);
        }
    }


    @Override
    @Transactional(transactionManager = "activationTransactionManager")
    public ActivationResponse activationRequest(ActivationRequest request) {
        ActivationRequestHeader headers = request.getRequestHeaders();
        clientVersionService.checkAppSignature(headers.getAppVersion(), headers.getSignature());
        List<Register> registerList = pwaUserRegisterService.findByPhoneNumber(request.getPhoneNumber());
        pwaUserRegisterService.checkIfBlocked(registerList);
        pwaUserRegisterService.checkRecentOtpSent(registerList);
        User user = findUser(request).orElseThrow(() -> {
            pwaUserRegisterService.checkTrials(request);
            return new GeneralPwaOauthException(CLIENT_NOT_FOUND);
        });
        UserActivation userActivation = activationService.save(request);
        log.info("User activation OTP created for phoneNumber={}", maskPhone(userActivation.getPhoneNumber()));
        pwaNotificationCenter.sendActivationOtp(user, request, userActivation);
        pwaUserRegisterService.saveRegistry(request, AuthStatus.OTP_SENT);
        return responseMapper.getMessage(PwaOauthMessage.CLIENT_REGISTRATION_SENT);
    }

    private Optional<User> findUser(ActivationRequest request) {
        String username = request.getUsername();
        String phoneNumber = request.getPhoneNumber();
        return userRepository
                .findByNicknameAndLegacyTerminalId(username, Integer.valueOf(TerminalType.MB.getLegacyTerminalId()))
                .stream()
                .filter(user -> user.getStatus().equals(UserStatus.ACTIVE) && String.join("", user.getAccessParameters()).contains(StringUtils.trim(phoneNumber)))
                .findFirst()
                .map(userMapper::toModel);
    }

    private String maskPhone(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            return null;
        }
        String text = phoneNumber.trim();
        if (text.length() <= 4) {
            return "****";
        }
        return "***" + text.substring(text.length() - 4);
    }

}
