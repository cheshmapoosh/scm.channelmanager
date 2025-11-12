package ir.daneshrefah.scm.otp.service;

import ir.daneshrefah.scm.common.constant.otp.OtpReason;
import ir.daneshrefah.scm.common.exception.InvalidInputException;
import ir.daneshrefah.scm.common.exception.MissingRequiredInputException;
import ir.daneshrefah.scm.common.model.message.MessageInput;
import ir.daneshrefah.scm.otp.config.RestClientUtils;
import ir.daneshrefah.scm.otp.dto.VerifyOTORequest;
import ir.daneshrefah.scm.otp.dto.VerifyOTOResponse;
import ir.daneshrefah.scm.otp.exception.InvalidPasswordException;
import ir.daneshrefah.scm.utils.MessageInputContext;
import ir.daneshrefah.scm.utils.constant.Constants;
import ir.daneshrefah.scm.utils.string.StringUtils;
import ir.daneshrefah.scm.utils.validation.ValidationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Objects;

import static ir.daneshrefah.scm.utils.constant.Constants.SCM_PARAMETER_ACCESS_PARAMETER;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "scm.otp.rest-client", name = "enabled", havingValue = "true")
public class RestOtpClientServiceImpl implements OtpClientService {

    private final RestClient restClient = RestClientUtils.createRestClient();
    @Value("${scm.otp.rest-client.auth-header-name}")
    private String authorizationHeader;
    @Value("${scm.otp.rest-client.auth-prefix}")
    private String bearerPrefix;
    @Value("${scm.otp.rest-client.base-url}")
    private String baseUrl;
    @Value("${scm.otp.rest-client.verify-logged-in-url}")
    private String verifyLoggedInUrl;

    @Override
    public boolean verifyOtpOrStaticPasswordLoggedInUser(String authorization, String otpCode, OtpReason reason, String accessParameter) {
        return getVerifyOTOResponseResponseEntity(authorization, otpCode, reason, accessParameter);
    }

    @Override
    public boolean verifyByCurrentToken(String otpCode, OtpReason reason) {
        MessageInput<?> messageInput = MessageInputContext.getCurrentContext();
        String authorization = messageInput.getAuthenticationValue();
        String accessParameter = messageInput.getHeader(SCM_PARAMETER_ACCESS_PARAMETER);
        return getVerifyOTOResponseResponseEntity(authorization, otpCode, reason, accessParameter);
    }

    private boolean getVerifyOTOResponseResponseEntity(String authorization, String otpCode, OtpReason reason, String accessParameter) {
        ValidationUtils.checkBlankString(authorization, () -> new MissingRequiredInputException("authorization"));
        ValidationUtils.checkBlankString(otpCode, () -> new MissingRequiredInputException("otpCode"));
        ValidationUtils.checkNull(reason, () -> new MissingRequiredInputException("reason"));
        ValidationUtils.checkBlankString(accessParameter, () -> new MissingRequiredInputException("accessParameter"));
        if (StringUtils.isBlank(authorization)) {
            throw new InvalidInputException(authorizationHeader);
        }
        if (!authorization.toUpperCase().startsWith(bearerPrefix.toUpperCase())) {
            authorization = bearerPrefix + authorization;
        }

        String fullUrl = baseUrl + verifyLoggedInUrl;

        VerifyOTORequest verifyOTORequest = VerifyOTORequest.builder()
                .otpType("SMS")
                .reason(reason.name())
                .claimCode(otpCode)
                .build();
        try {
            VerifyOTOResponse verifyOTOResponse = restClient.post()
                    .uri(fullUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(Constants.SCM_PARAMETER_AUTHORIZATION, authorization)
                    .header(Constants.SCM_PARAMETER_ACCESS_PARAMETER, accessParameter)
                    .body(verifyOTORequest)
                    .exchange((clientRequest, clientResponse) -> {
                        if (clientResponse.getStatusCode().isSameCodeAs(HttpStatusCode.valueOf(HttpStatus.OK.value()))) {
                            return Objects.requireNonNull(clientResponse.bodyTo(VerifyOTOResponse.class));
                        } else if (clientResponse.getStatusCode().isSameCodeAs(HttpStatusCode.valueOf(HttpStatus.BAD_REQUEST.value()))) {
                            throw new InvalidPasswordException("password", "invalid password");
                        } else {
                            throw new RuntimeException("Unexpected error from OTP service: " + clientResponse.getStatusCode().value());
                        }
                    });
            return verifyOTOResponse.isSuccessful();
        } catch (Exception ex) {
            throw new RuntimeException("Error while calling verify OTP API", ex);
        }
    }

    @Override
    public void verifyOtpOrStaticPasswordLoggedInUserWithException(String authorization, String otpCode, OtpReason reason, String accessParameter) {
        if (!getVerifyOTOResponseResponseEntity(authorization, otpCode, reason, accessParameter)) {
            throw new InvalidPasswordException("password", "invalid password");
        }
    }
}
