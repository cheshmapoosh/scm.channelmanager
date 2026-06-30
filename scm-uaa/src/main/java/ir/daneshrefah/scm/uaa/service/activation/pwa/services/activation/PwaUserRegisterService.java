package ir.daneshrefah.scm.uaa.service.activation.pwa.services.activation;

import ir.daneshrefah.scm.uaa.common.constants.AuthStatus;
import ir.daneshrefah.scm.uaa.common.constants.PwaOauthMessage;
import ir.daneshrefah.scm.uaa.config.PwaAuthenticationConfigProperties;
import ir.daneshrefah.scm.uaa.domain.pwa.Register;
import ir.daneshrefah.scm.uaa.mapper.RegisterMapper;
import ir.daneshrefah.scm.uaa.repository.activation.RegisterRepository;
import ir.daneshrefah.scm.uaa.repository.activation.domain.RegisterEntity;
import ir.daneshrefah.scm.uaa.service.activation.pwa.common.GeneralPwaOauthException;
import ir.daneshrefah.scm.uaa.service.activation.pwa.model.ActivationRequest;
import ir.daneshrefah.scm.uaa.service.activation.pwa.model.ActivationRequestHeader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@ConditionalOnBean(name = "activationDataSource")
@RequiredArgsConstructor
@Slf4j
public class PwaUserRegisterService {

    private final RegisterRepository registerRepository;
    private final RegisterMapper registerMapper;
    private final PwaNotificationCenter pwaNotificationCenter;
    private final PwaAuthenticationConfigProperties properties;

    public void save(Register register) {
        log.info("Request to save register info for username={}", register == null ? null : register.getUsername());
        RegisterEntity entity = registerMapper.toEntity(register);
        registerRepository.save(entity);
    }


    public List<Register> findByPhoneNumber(String phoneNumber) {
        log.info("Request to find registration info for phoneNumber={}", maskPhone(phoneNumber));
        List<RegisterEntity> registerList = registerRepository.findTop20ByPhoneNumberOrderByLastRegisterDesc(phoneNumber);
        return registerList.stream().map(registerMapper::toMoel).collect(Collectors.toList());
    }


    public void saveRegistry(ActivationRequest request, AuthStatus authStatus) {
        log.info("Set {} register status for phoneNumber={}", request.getUsername(), maskPhone(request.getPhoneNumber()));
        Register registeredClient = createRegisteredClient(request);
        registeredClient.setStatus(authStatus);
        save(registeredClient);
    }

    private Register createRegisteredClient(ActivationRequest request) {
        ActivationRequestHeader headers = request.getRequestHeaders();
        Register register = new Register();
        register.setAgent(headers.getAgent());
        register.setUsername(request.getUsername());
        register.setDeviceModel(headers.getDeviceModel());
        register.setPhoneNumber(request.getPhoneNumber());
        register.setAppVersion(headers.getAppVersion());
        register.setLastRegister(ZonedDateTime.now());
        register.setIp(headers.getIp());
        return register;
    }

    private List<Register> getRecentFailedRegister(List<Register> registryHistory) {
        return registryHistory
                .stream()
                .filter(register -> !Objects.equals(register.getStatus(), AuthStatus.SUCCEEDED))
                .toList();
    }

    @Transactional(transactionManager = "activationTransactionManager", propagation = Propagation.REQUIRES_NEW)
    public void checkTrials(ActivationRequest request) {
        Register register = createRegisteredClient(request);
        log.info("Check recent failed trials to register for phoneNumber={}", maskPhone(register.getPhoneNumber()));
        List<Register> registryHistory = findByPhoneNumber(register.getPhoneNumber());
        List<Register> recentRegister = getRecentFailedRegister(registryHistory);
        long failedTrials = recentRegister
                .stream()
                .filter(r -> !Objects.equals(r.getStatus(), AuthStatus.BLOCKED))
                .count();
        if ((failedTrials >= properties.getActivation().rateLimitCount())) {
            log.info("Blocking user with phoneNumber={} in register process", maskPhone(register.getPhoneNumber()));
            register.setBlockedTime(ZonedDateTime.now());
            register.setStatus(AuthStatus.BLOCKED);
            save(register);
            log.debug("Sending block SMS in registration process to phoneNumber={}", maskPhone(register.getPhoneNumber()));
            pwaNotificationCenter.sendActivationBlockedNotification(request, properties.getActivation().rateLimitCount().toString(), properties.getActivation().rateLimitBlockedTimeMinutes().toString());
        } else {
            log.debug("Adding failed register trial for user with phoneNumber={}", maskPhone(register.getPhoneNumber()));
            register.setStatus(AuthStatus.NOT_FOUND);
            save(register);
        }
    }


    public void checkIfBlocked(List<Register> registryHistory) {
        List<Register> recentRegister = getRecentFailedRegister(registryHistory);
        Register lastRegister;
        if (!recentRegister.isEmpty()) {
            lastRegister = recentRegister.get(0);
            log.info("Previous registration status={}", lastRegister.getStatus());
            if (Objects.nonNull(lastRegister.getBlockedTime())) {
                if (!Duration.ofMinutes(properties.getActivation().rateLimitBlockedTimeMinutes()).minus(Duration.between(lastRegister.getBlockedTime(), ZonedDateTime.now())).isNegative()) {
                    log.info("PhoneNumber={} is blocked to register for a temporary state", maskPhone(lastRegister.getPhoneNumber()));
                    throw new GeneralPwaOauthException(PwaOauthMessage.REACHED_LOGIN_LIMIT);
                }
            }
        }
    }

    public void checkRecentOtpSent(List<Register> registryHistory) {
        if (!registryHistory.isEmpty()) {
            log.info("Fetch recent otp send info for phoneNumber={}", maskPhone(registryHistory.get(0).getPhoneNumber()));
            Register lastRegisterInfo = registryHistory.get(0);
            boolean isOtpOld = Duration.ofMinutes(properties.getActivation().otpCodeExpirationMinutes()).minus(Duration.between(lastRegisterInfo.getLastRegister(), ZonedDateTime.now())).isNegative();
            if (lastRegisterInfo.getStatus().equals(AuthStatus.OTP_SENT) && !isOtpOld) {
                log.info("PhoneNumber={} has unexpired registration token and is inactive yet", maskPhone(lastRegisterInfo.getPhoneNumber()));
                throw new GeneralPwaOauthException(PwaOauthMessage.REGISTRATION_ALREADY_SENT);
            }
        }
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
