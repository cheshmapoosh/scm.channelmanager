package ir.daneshrefah.scm.uaa.service.activation.pwa.services.activation;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import ir.daneshrefah.scm.uaa.common.constants.PwaOauthMessage;
import ir.daneshrefah.scm.uaa.common.utils.ErrorUtils;
import ir.daneshrefah.scm.uaa.config.PwaAuthenticationConfigProperties;
import ir.daneshrefah.scm.uaa.domain.pwa.UserActivation;
import ir.daneshrefah.scm.uaa.mapper.UserActivationMapper;
import ir.daneshrefah.scm.uaa.repository.activation.DeviceClientRepository;
import ir.daneshrefah.scm.uaa.repository.activation.UserActivationRepository;
import ir.daneshrefah.scm.uaa.repository.activation.domain.DeviceClientEntity;
import ir.daneshrefah.scm.uaa.repository.activation.domain.UserActivationEntity;
import ir.daneshrefah.scm.uaa.security.token.DefaultGrantPreAuthenticationToken;
import ir.daneshrefah.scm.uaa.security.token.PreAuthenticationToken;
import ir.daneshrefah.scm.uaa.service.activation.pwa.common.GeneralPwaOauthException;
import ir.daneshrefah.scm.uaa.service.activation.pwa.model.ActivationRequest;
import ir.daneshrefah.scm.uaa.service.activation.pwa.model.ActivationRequestHeader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Cipher;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Objects;

import static ir.daneshrefah.scm.uaa.common.constants.PwaOauthMessage.CLIENT_INACTIVE;
import static ir.daneshrefah.scm.uaa.common.constants.PwaOauthMessage.CLIENT_INVALID_APP_VERSION;
import static ir.daneshrefah.scm.uaa.common.utils.Constants.OAUTH2_ERROR_CODE_IS_DISABLED;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserActivationService {

    private final UserActivationMapper userActivationMapper;
    private final UserActivationRepository userActivationRepository;
    private final DeviceClientRepository deviceClientRepository;
    private final JWKSet jwkSet;
    private final PwaAuthenticationConfigProperties properties;

    @Transactional
    public UserActivation save(ActivationRequest request) {
        ActivationRequestHeader headers = request.getRequestHeaders();
        //TODO CHANGE ON FUTURE WITH SCM CLIENT_VERSION
        DeviceClientEntity deviceClient = deviceClientRepository.findByAppVersion(headers.getAppVersion()).orElseThrow(() -> new GeneralPwaOauthException(CLIENT_INVALID_APP_VERSION));
        UserActivationEntity userActivation = createUserActivation(request, deviceClient);
        UserActivationEntity saved = userActivationRepository.save(userActivation);
        return userActivationMapper.toModel(saved);
    }

    public UserActivation findLastActivationRequest(ActivationRequest request) {
        return userActivationRepository
                .findFirstByUsernameAndPhoneNumberOrderByLastUsedDesc(request.getUsername(), request.getPhoneNumber())
                .map(userActivationMapper::toModel)
                .orElseThrow(() -> new GeneralPwaOauthException(CLIENT_INACTIVE));
    }

    private UserActivationEntity createUserActivation(ActivationRequest request, DeviceClientEntity deviceClient) {
        ActivationRequestHeader headers = request.getRequestHeaders();
        UserActivationEntity userActivation = new UserActivationEntity();
        userActivation.setUsername(request.getUsername());
        userActivation.setUuid(headers.getUuid().toString());
        userActivation.setPhoneNumber(request.getPhoneNumber());
        userActivation.setChannel(StringUtils.toRootUpperCase(headers.getChannel()));
        userActivation.setActivated(false);
        userActivation.setAgent(headers.getAgent());
        userActivation.setDeviceModel(headers.getDeviceModel());
        userActivation.setOsVersion(headers.getOsVersion());
        userActivation.setLastUsed(ZonedDateTime.now());
        userActivation.setRetryCount(0);
        userActivation.setDeviceClient(deviceClient);
        userActivation.setTokenSetTime(ZonedDateTime.now());
        userActivation.setCodeValid(true);
        userActivation.setActivationCode(createOtpCode());
        userActivation.setRegistryToken(generateRegistryToken(request.getUsername(), request.getPhoneNumber(), headers.getAgent()));
        log.info("Created user activation for username={}", userActivation.getUsername());
        return userActivation;
    }

    private String createOtpCode() {
        SecureRandom random = new SecureRandom();
        int num = random.nextInt(1000000);
        return String.format("%06d", num);
    }

    private String generateRegistryToken(String username, String phoneNumber, String agent) {
        try {
            String plainRegistry = username + phoneNumber + agent + System.currentTimeMillis();
            /* Generate private key. */
            JWK jwk = jwkSet.getKeys().get(0);
            RSAKey rsaKey = (RSAKey) jwk;
            PrivateKey privateKey = rsaKey.toPrivateKey();
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, privateKey);
            byte[] encryptedBytes = cipher.doFinal(plainRegistry.getBytes());
            return new String(Base64.getEncoder().encode(encryptedBytes));
        } catch (Exception e) {
            log.error("could not encrypt user registry data due to error:{}", e.getMessage());
            return null;
        }
    }

    public boolean validateActivationCode(ActivationRequest request, UserActivation userActivation) {
        log.info("Checking code expiration for user: {}", userActivation.getUsername());
        boolean hasExpiration = !Duration.ofMinutes(properties.getActivation().otpCodeExpirationMinutes()).minus(Duration.between(userActivation.getTokenSetTime(), ZonedDateTime.now())).isNegative();
        log.info("Checking code validation for user: {}", userActivation.getUsername());
        if (hasExpiration) {
            if (Objects.equals(userActivation.getCodeValid(), true))
                return !isClientRestricted(userActivation, request);
            else
                throw new GeneralPwaOauthException(PwaOauthMessage.REACHED_TRIAL_LIMIT);
        } else {
            return false;
        }
    }

    private void setTokenValidity(UserActivation userActivation) {
        log.info("Request to update user activation code validity id={}", userActivation.getId());
        userActivationRepository.updateClientCodeValidity(userActivation.getId(), userActivation.getCodeValid());
    }

    private void setRetryCount(UserActivation userActivation) {
        log.info("Request to update user activation retry count id={}", userActivation.getId());
        userActivationRepository.updateClientRetryCount(userActivation.getId(), userActivation.getRetryCount());
    }

    private boolean isClientRestricted(UserActivation userActivation, ActivationRequest request) {
        log.info("Checking if  user {} is restricted", userActivation.getUsername());
        Objects.requireNonNull(userActivation.getActivationCode());
        if (!userActivation.getActivationCode().equals(request.getOtpCode())) {
            userActivation.setRetryCount(userActivation.getRetryCount() + 1);
            if (userActivation.getRetryCount() >= properties.getActivation().otpCodeTrailsCount()) {
                userActivation.setCodeValid(false);
                setTokenValidity(userActivation);
                log.info("Inactivating registration token for phoneNumber={}", maskPhone(userActivation.getPhoneNumber()));
                throw new GeneralPwaOauthException(PwaOauthMessage.REACHED_TRIAL_LIMIT);
            }
            setRetryCount(userActivation);
            return true;
        } else
            return false;
    }

    public void updateActivationStatus(UserActivation userActivation) {
        log.info("Request to update user activation status id={}", userActivation.getId());
        userActivationRepository.updateActivationStatus(userActivation.getId(), userActivation.getActivated());
    }

    public void  checkActivationIfNeeded(PreAuthenticationToken token) {
        if (properties.getActivation().checkRegistryToken()) {
            DefaultGrantPreAuthenticationToken defaultGrantToken = token.getDefaultGrantPreAuthToken();
            List<UserActivationEntity> resultList = userActivationRepository.findAllByUsernameAndPhoneNumberAndRegistryTokenOrderByLastUsedDesc(token.getUsername(), defaultGrantToken.getAccessParam(), defaultGrantToken.getRegistryToken());
            resultList
                    .stream()
                    .filter(u->u.getRegistryToken().equals(token.getDefaultGrantPreAuthToken().getRegistryToken()))
                    .filter(u-> Objects.equals(u.getActivated(), true))
                    .findFirst()
                    .ifPresentOrElse(f->{
                        log.info("New active client by username '{}'", token.getUsername());

                    },()->{
                        log.info("New inactive client by username '{}' ", token.getUsername());
                        ErrorUtils.throwError(OAUTH2_ERROR_CODE_IS_DISABLED, PwaOauthMessage.CLIENT_INACTIVE.name());
                    });
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
