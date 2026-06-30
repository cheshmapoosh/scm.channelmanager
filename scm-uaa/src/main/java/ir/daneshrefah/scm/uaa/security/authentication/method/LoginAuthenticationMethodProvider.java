package ir.daneshrefah.scm.uaa.security.authentication.method;

import ir.daneshrefah.scm.common.constant.otp.OtpReason;
import ir.daneshrefah.scm.common.constant.otp.OtpType;
import ir.daneshrefah.scm.common.model.recipient.Recipient;
import ir.daneshrefah.scm.common.model.user.UserIdentifierType;
import ir.daneshrefah.scm.uaa.common.exception.TwoStepAuthenticationRequiredException;
import ir.daneshrefah.scm.uaa.common.model.user.User;
import ir.daneshrefah.scm.uaa.common.security.authenticationDetails.TerminalUserDetails;
import ir.daneshrefah.scm.uaa.security.oauth2.error.OAuth2AuthenticationErrorMapper;
import ir.daneshrefah.scm.uaa.security.password.LegacyPassword;
import ir.daneshrefah.scm.uaa.security.authentication.method.token.DeviceOtpRequestLoginAuthenticationToken;
import ir.daneshrefah.scm.uaa.security.authentication.method.token.DeviceOtpVerifyLoginAuthenticationToken;
import ir.daneshrefah.scm.uaa.security.authentication.method.token.PatternLoginAuthenticationToken;
import ir.daneshrefah.scm.uaa.security.authentication.method.token.PinLoginAuthenticationToken;
import ir.daneshrefah.scm.uaa.security.authentication.method.token.SmsOtpRequestLoginAuthenticationToken;
import ir.daneshrefah.scm.uaa.security.authentication.method.token.SmsOtpVerifyLoginAuthenticationToken;
import ir.daneshrefah.scm.uaa.security.authentication.method.token.StaticPasswordLoginAuthenticationToken;
import ir.daneshrefah.scm.uaa.security.authentication.token.UserLoginAuthenticationToken;
import ir.daneshrefah.scm.uaa.security.authentication.token.AuthenticationOutcomeToken;
import ir.daneshrefah.scm.uaa.service.otp.OtpService;
import ir.daneshrefah.scm.uaa.service.otp.dto.OtpSendRequest;
import ir.daneshrefah.scm.uaa.service.otp.dto.OtpSendResponse;
import ir.daneshrefah.scm.uaa.service.otp.dto.OtpVerifyRequest;
import ir.daneshrefah.scm.uaa.service.otp.dto.OtpVerifyResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

import static ir.daneshrefah.scm.uaa.common.utils.Constants.OAUTH2_ERROR_CODE_INVALID_CLAIM;

@Component
public class LoginAuthenticationMethodProvider extends LoginAuthenticationMethodProviderSupport {
    private static final Set<Class<?>> SUPPORTED_TOKENS = Set.of(
            StaticPasswordLoginAuthenticationToken.class,
            PinLoginAuthenticationToken.class,
            PatternLoginAuthenticationToken.class,
            SmsOtpRequestLoginAuthenticationToken.class,
            SmsOtpVerifyLoginAuthenticationToken.class,
            DeviceOtpRequestLoginAuthenticationToken.class,
            DeviceOtpVerifyLoginAuthenticationToken.class
    );

    private final PasswordEncoder passwordEncoder;
    private final OtpService otpService;

    public LoginAuthenticationMethodProvider(
            OAuth2AuthenticationErrorMapper errorMapper,
            PasswordEncoder passwordEncoder,
            OtpService otpService
    ) {
        super(errorMapper);
        this.passwordEncoder = passwordEncoder;
        this.otpService = otpService;
    }

    @Override
    protected void additionalAuthenticationChecks(
            TerminalUserDetails userDetails,
            UserLoginAuthenticationToken authentication
    ) throws AuthenticationException {
        if (requiresStaticPassword(authentication)) {
            validateStaticPassword(userDetails, authentication);
        }
        if (authentication instanceof SmsOtpVerifyLoginAuthenticationToken) {
            verifySmsOtp(userDetails, authentication, true);
        } else if (authentication instanceof DeviceOtpVerifyLoginAuthenticationToken) {
            verifyDeviceOtp(userDetails, authentication);
        }
    }

    @Override
    protected Authentication createSuccessAuthentication(UserLoginAuthenticationToken authentication) {
        if (authentication instanceof SmsOtpRequestLoginAuthenticationToken) {
            return incomplete(authentication, OtpType.SMS);
        }
        if (authentication instanceof DeviceOtpRequestLoginAuthenticationToken) {
            return incomplete(authentication, OtpType.DEVICE);
        }
        return super.createSuccessAuthentication(authentication);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return SUPPORTED_TOKENS.stream().anyMatch(type -> type.isAssignableFrom(authentication));
    }

    private boolean requiresStaticPassword(UserLoginAuthenticationToken authentication) {
        return authentication instanceof StaticPasswordLoginAuthenticationToken
                || authentication instanceof PinLoginAuthenticationToken
                || authentication instanceof PatternLoginAuthenticationToken
                || authentication instanceof SmsOtpRequestLoginAuthenticationToken
                || authentication instanceof DeviceOtpRequestLoginAuthenticationToken;
    }

    private void validateStaticPassword(
            TerminalUserDetails userDetails,
            UserLoginAuthenticationToken authentication
    ) {
        Object credentials = authentication.getCredentials();
        if (credentials == null) {
            logger.debug("Failed to authenticate since no credentials were provided");
            throw new BadCredentialsException("LoginAuthenticationMethodProvider.badCredentials");
        }
        String storedPassword = userDetails.getPassword();
        String legacyUsernameSalt = userDetails.getUser().getPerson().getUsername();
        if (storedPassword == null || !passwordEncoder.matches(
                new LegacyPassword(credentials.toString(), legacyUsernameSalt),
                storedPassword
        )) {
            logger.debug("Failed to authenticate since password does not match stored value");
            throw new BadCredentialsException("LoginAuthenticationMethodProvider.badCredentials");
        }
    }

    private Authentication incomplete(UserLoginAuthenticationToken authentication, OtpType otpType) {
        OtpSendResponse otpSendResponse = requestOtp(authentication, otpType);
        AuthenticationOutcomeToken result = AuthenticationOutcomeToken.incomplete(
                authentication.getPrincipal(),
                authentication.getDetails(),
                otpSendResponse
        );
        logger.debug("Authenticated user; second step is required");
        return result;
    }

    private OtpSendResponse requestOtp(UserLoginAuthenticationToken authentication, OtpType otpType) {
        User user = authentication.getPrincipal().getUser();
        Recipient recipient = Recipient.builder()
                .address(user.getPerson().getMobile1())
                .identifier(user.getNickname())
                .identifierType(UserIdentifierType.USER_NICKNAME)
                .terminalCode(user.getTerminalCode())
                .accessParameter(authentication.getDetails().getAccessParameter())
                .build();
        OtpSendRequest otpRequest = OtpSendRequest.builder()
                .otpType(otpType)
                .reason(OtpReason.AUTHENTICATION)
                .recipient(recipient)
                .build();
        return otpService.sendOtp(otpRequest);
    }

    private void verifySmsOtp(
            TerminalUserDetails userDetails,
            UserLoginAuthenticationToken authentication,
            boolean wrapTwoStepError
    ) {
        Recipient recipient = Recipient.builder()
                .address(userDetails.getUser().getPerson().getMobile1())
                .identifier(userDetails.getUser().getNickname())
                .identifierType(UserIdentifierType.USER_NICKNAME)
                .terminalCode(userDetails.getUser().getTerminalCode())
                .accessParameter(authentication.getDetails().getAccessParameter())
                .build();
        OtpVerifyRequest request = OtpVerifyRequest.builder()
                .otpType(OtpType.SMS)
                .reason(OtpReason.AUTHENTICATION)
                .recipient(recipient)
                .claimCode(authentication.getDetails().getClaimCode())
                .build();
        OtpVerifyResponse otpVerifyResponse = otpService.verifyOtp(request);
        if (!otpVerifyResponse.isSuccessful()) {
            BadCredentialsException exception = new BadCredentialsException(OAUTH2_ERROR_CODE_INVALID_CLAIM);
            if (wrapTwoStepError) {
                throw new TwoStepAuthenticationRequiredException(authentication, exception);
            }
            throw exception;
        }
    }

    private void verifyDeviceOtp(
            TerminalUserDetails userDetails,
            UserLoginAuthenticationToken authentication
    ) {
        OtpVerifyRequest request = OtpVerifyRequest.builder()
                .user(userDetails.getUser())
                .otpType(OtpType.DEVICE)
                .reason(OtpReason.AUTHENTICATION)
                .recipient(Recipient.builder().terminalCode(userDetails.getUser().getTerminalCode()).build())
                .claimCode(authentication.getDetails().getClaimCode())
                .build();
        OtpVerifyResponse otpVerifyResponse = otpService.verifyOtp(request);
        if (!otpVerifyResponse.isSuccessful()) {
            throw new BadCredentialsException(OAUTH2_ERROR_CODE_INVALID_CLAIM);
        }
    }
}
