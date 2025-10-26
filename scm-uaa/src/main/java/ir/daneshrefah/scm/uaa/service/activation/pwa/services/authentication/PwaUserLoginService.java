package ir.daneshrefah.scm.uaa.service.activation.pwa.services.authentication;

import ir.daneshrefah.scm.uaa.common.constants.AuthStatus;
import ir.daneshrefah.scm.uaa.common.constants.PwaOauthMessage;
import ir.daneshrefah.scm.uaa.common.utils.ErrorUtils;
import ir.daneshrefah.scm.uaa.config.PwaAuthenticationConfigProperties;
import ir.daneshrefah.scm.uaa.domain.pwa.PwaLogin;
import ir.daneshrefah.scm.uaa.mapper.UserLoginMapper;
import ir.daneshrefah.scm.uaa.repository.activation.UserLoginRepository;
import ir.daneshrefah.scm.uaa.repository.activation.domain.PwaLoginEntity;
import ir.daneshrefah.scm.uaa.service.activation.pwa.services.activation.PwaNotificationCenter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static ir.daneshrefah.scm.uaa.common.constants.SMSMessageType.MB_LOGIN_BLOCKED;
import static ir.daneshrefah.scm.uaa.common.utils.Constants.OAUTH2_ERROR_CODE_LOGIN_LIMITED;

@RequiredArgsConstructor
@Service
@Slf4j
public class PwaUserLoginService {

    private final PwaAuthenticationConfigProperties properties;


    private final UserLoginMapper loginMapper;
    private final UserLoginRepository userLoginRepository;
    private final PwaNotificationCenter pwaNotificationCenter;

    public PwaLogin save(PwaLogin login) {
        PwaLoginEntity entity = loginMapper.toEntity(login);
        PwaLoginEntity saved = userLoginRepository.save(entity);
        return loginMapper.toModel(saved);
    }


    public List<PwaLogin> findByUsername(String username) {
        List<PwaLoginEntity> resultList = userLoginRepository.findTop10ByUsernameOrderByIdDesc(username);
        return resultList.stream().map(loginMapper::toModel).collect(Collectors.toList());
    }

    public void succeededTrial(PwaLogin login) {
        log.info("Set success login status for username: {}", login.getUsername());
        login.setStatus(AuthStatus.SUCCEEDED);
        save(login);
    }

    @Transactional(transactionManager = "activationTransactionManager", propagation = Propagation.REQUIRES_NEW)
    public boolean hasReachedLoginLimit(PwaLogin login) {
        log.info("Check recent failed trials to login for user: {}", login.getUsername());
        List<PwaLogin> recentRegister = getRecentFailedLogin(login.getUsername());
        long failedTrials = recentRegister
                .stream()
                .filter(r -> !Objects.equals(r.getStatus(), AuthStatus.BLOCKED))
                .count();
        if ((failedTrials >= properties.getLogin().rateLimitCount())) {
            log.info("Blocking user with username: {} in login process", login.getUsername());
            login.setBlockedTime(ZonedDateTime.now());
            login.setStatus(AuthStatus.BLOCKED);
            save(login);
            log.info("Sending block SMS in login process for username {} to phoneNumber {}", login.getUsername(), login.getPhoneNumber());
            pwaNotificationCenter.sendLoginBlockedNotification(login,properties.getLogin().rateLimitCount().toString(), properties.getLogin().rateLimitBlockedTimeMinutes().toString());
            return true;
        } else {
            log.info("Saving failed trial for user with username: {} in login process", login.getUsername());
            login.setStatus(AuthStatus.FAILED);
            save(login);
        }
        return false;
    }


    public void checkIfBlocked(String username) {
        List<PwaLogin> recentLogin = getRecentFailedLogin(username);
        PwaLogin lastLogin;
        if (!recentLogin.isEmpty()) {
            lastLogin = recentLogin.get(0);
            if (Objects.nonNull(lastLogin.getBlockedTime())) {
                if (!Duration.ofMinutes(properties.getLogin().rateLimitBlockedTimeMinutes()).minus(Duration.between(lastLogin.getBlockedTime(), ZonedDateTime.now())).isNegative()) {
                    log.error("Username: {} is blocked to login for a temporary state", lastLogin.getUsername());
                    ErrorUtils.throwError(OAUTH2_ERROR_CODE_LOGIN_LIMITED, PwaOauthMessage.REACHED_LOGIN_LIMIT.name());
                }
            }
        }
    }

    private List<PwaLogin> getRecentFailedLogin(String username) {
        log.info("Fetch recent login info for user: {}", username);
        List<PwaLogin> lastLogin = new ArrayList<>();
        List<PwaLogin> loginList = findByUsername(username);
        for (PwaLogin login : loginList) {
            if (login.getStatus().equals(AuthStatus.SUCCEEDED))
                break;
            lastLogin.add(login);
        }
        return lastLogin;
    }

}
