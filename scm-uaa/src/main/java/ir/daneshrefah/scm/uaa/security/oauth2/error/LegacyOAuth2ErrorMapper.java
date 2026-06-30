package ir.daneshrefah.scm.uaa.security.oauth2.error;

import ir.daneshrefah.scm.uaa.common.exception.TwoStepAuthenticationRequiredException;
import ir.daneshrefah.scm.uaa.security.authentication.token.AuthenticationOutcomeToken;
import ir.daneshrefah.scm.uaa.service.otp.dto.OtpSendResponse;
import ir.daneshrefah.scm.utils.date.DateUtils;
import ir.daneshrefah.scm.utils.string.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class LegacyOAuth2ErrorMapper {
    private final OAuth2AuthenticationErrorMapper errorMapper;

    public LegacyError map(Exception exception) {
        String parameterName = errorMapper.parameterName(exception);
        String errorCode = legacyCompatibilityErrorCode(exception);
        if (StringUtils.isEmpty(errorCode)) {
            errorCode = parameterName;
        }
        return new LegacyError(errorCode, parameterName);
    }

    private String legacyCompatibilityErrorCode(Exception exception) {
        if (exception == null || exception.getCause() != null) {
            return null;
        }
        if (exception instanceof TwoStepAuthenticationRequiredException twoStepException) {
            if (!(twoStepException.getAuthentication() instanceof AuthenticationOutcomeToken authenticationToken)) {
                return null;
            }
            String code = authenticationToken.getPrincipal().getUser().getLoginAuthenticationMethod().getCode();
            Map<String, String> response = new HashMap<>();
            if (Objects.nonNull(authenticationToken.getOtpSendResponse())) {
                OtpSendResponse otpSendResponse = authenticationToken.getOtpSendResponse();
                Instant expireTimeInstant = otpSendResponse.getOtp().getExpireTime();
                LocalDateTime nowLocalDateTime = LocalDateTime.now();
                LocalDateTime expirationLocalDateTime = DateUtils.DateConverter.convertToLocalDateTime(
                        DateUtils.DateConverter.convertToTimestamp(expireTimeInstant)
                );
                response.put("expirationDurationSeconds",
                        String.valueOf(Duration.between(nowLocalDateTime, expirationLocalDateTime).toSeconds()));
                response.put("recipient", safeRecipient(otpSendResponse.getOtp().getRecipient().getAddress()));
            }
            response.put("authenticationMethod", code);
            return response.toString().replace("=", StringUtils.COLON);
        }
        return null;
    }

    private String safeRecipient(String recipient) {
        if (recipient == null || recipient.length() < 4) {
            return "****";
        }
        return "***" + recipient.substring(recipient.length() - 4);
    }

    public record LegacyError(String errorCode, String parameterName) {
    }
}
