package ir.daneshrefah.scm.uaa.common.utils;

import ir.daneshrefah.scm.common.model.message.ClientAuthenticationType;
import ir.daneshrefah.scm.common.model.message.IssuerInfo;
import ir.daneshrefah.scm.uaa.common.model.authentication.UserAuthentication;
import ir.daneshrefah.scm.uaa.common.model.user.User;
import ir.daneshrefah.scm.utils.MessageInputContext;
import ir.daneshrefah.scm.utils.string.StringUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Objects;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-02-12
 */
public class AuthenticationUtils {

    public static UserAuthentication getLoggedInUserAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (null == authentication || !authentication.isAuthenticated() ||
                !authentication.getClass().isAssignableFrom(UserAuthentication.class)) {
            return null;
        }
        return (UserAuthentication) authentication;
    }

    public static User getLoggedInUser() {
        UserAuthentication authentication = getLoggedInUserAuthentication();
        if (null == authentication ||
                !authentication.getPrincipal().getClass().isAssignableFrom(User.class)) {
            return null;
        }
        return authentication.getPrincipal();
    }

    public static String getLoggedInGlobalUsername() {
        User user = getLoggedInUser();
        return null == user ? null : user.getPerson().getUsername();
    }

    public static IssuerInfo getIssuerInfo() {
        UserAuthentication authentication = getLoggedInUserAuthentication();
        return IssuerInfo.builder()
                .parentCorrelationId(MessageInputContext.getCurrentContext().getCorrelationId())
                .personType(authentication.getPrincipal().getPerson().getPersonType())
                .personUsername(authentication.getPrincipal().getPerson().getUsername())
//                .identifierType(UserIdentifierType.USER_NICKNAME)
                .terminalCode(authentication.getTerminalCode())
//                .remoteAddress()
//                .xForwardedFor()
//                .hostAddress()
//                .instanceName()
                .build();
    }

    public static ClientAuthenticationType extractAuthenticationType(String authorizationHeader, String username, String credential) {
        if (StringUtils.isEmpty(authorizationHeader)) {
            return ClientAuthenticationType.ANONYMOUS;
        }

        String AUTHENTICATION_SCHEME_BASIC = "Basic";
        String AUTHENTICATION_SCHEME_BEARER = "Bearer";
        String AUTHENTICATION_SCHEME_SESSION = "Session";

        if (StringUtils.startsWithIgnoreCase(authorizationHeader, AUTHENTICATION_SCHEME_BASIC)) {
            return ClientAuthenticationType.CLIENT;
        } else if (StringUtils.startsWithIgnoreCase(authorizationHeader, AUTHENTICATION_SCHEME_SESSION)) {
            return ClientAuthenticationType.SESSION;
        } else if (StringUtils.startsWithIgnoreCase(authorizationHeader, AUTHENTICATION_SCHEME_BEARER)) {
            return ClientAuthenticationType.BEARER;
        } else {
            if (StringUtils.isNotEmpty(username) && StringUtils.isNotEmpty(credential)) {
                return ClientAuthenticationType.BASIC;
            }
        }
        return ClientAuthenticationType.ANONYMOUS;
    }

    public static String extractAuthenticationValue(String authorizationHeader) {
        if (StringUtils.isEmpty(authorizationHeader)) {
            return null;
        }
        String[] args = authorizationHeader.split(" ");
        if (args.length < 2)
            return null;
        return args[1];
    }


    public static boolean isFullyAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (Objects.isNull(authentication) || !authentication.isAuthenticated()) {
            return false;
        }
        if (!(authentication instanceof ir.daneshrefah.scm.common.model.message.Authentication)) {
            return false;
        }
        return ((ir.daneshrefah.scm.common.model.message.Authentication) authentication).isFullyAuthenticated();
    }

    public static Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    public static ir.daneshrefah.scm.common.model.message.Authentication getScmAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return Objects.nonNull(authentication) && authentication instanceof ir.daneshrefah.scm.common.model.message.Authentication ?
                (ir.daneshrefah.scm.common.model.message.Authentication) authentication : null;
    }

    public static boolean isTransactionAuthenticated() {
        ir.daneshrefah.scm.common.model.message.Authentication authentication = getScmAuthentication();
        return authentication.isFullyAuthenticated() && authentication.getIsTransactionAuthenticated();
    }

    public static boolean isTransactionAuthenticationInitialized() {
        ir.daneshrefah.scm.common.model.message.Authentication authentication = getScmAuthentication();
        return authentication.isFullyAuthenticated() && authentication.getIsTransactionAuthenticated();
    }

    public static void authenticateTransaction(boolean isAuthenticated) {
        ir.daneshrefah.scm.common.model.message.Authentication authentication = getScmAuthentication();
        if (Objects.isNull(authentication) || !authentication.isFullyAuthenticated()) {
            throw new RuntimeException("invalid operation for authenticateTransaction");
        }
        authentication.authenticateTransaction(isAuthenticated);
    }

}
