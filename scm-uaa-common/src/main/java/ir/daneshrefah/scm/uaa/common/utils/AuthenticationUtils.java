package ir.daneshrefah.scm.uaa.common.utils;

import ir.daneshrefah.scm.common.model.message.IssuerInfo;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.user.AuthenticationLevel;
import ir.daneshrefah.scm.common.model.user.UserIdentifierType;
import ir.daneshrefah.scm.uaa.common.model.authentication.UserAuthentication;
import ir.daneshrefah.scm.uaa.common.model.user.User;
import ir.daneshrefah.scm.utils.MessageContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-02-12
 */
public class AuthenticationUtils {

    public static UserAuthentication getLoggedInUserAuthentication(Message message) {
        ir.daneshrefah.scm.common.model.message.Authentication authentication = message.getHeader().getAuthentication();
        if (null == authentication || !authentication.isAuthenticated() ||
                !authentication.getClass().isAssignableFrom(UserAuthentication.class)) {
            return null;
        }
        return (UserAuthentication) authentication;
    }

    public static UserAuthentication getLoggedInUserAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (null == authentication || !authentication.isAuthenticated() ||
                !authentication.getClass().isAssignableFrom(UserAuthentication.class)) {
            return null;
        }
        return (UserAuthentication) authentication;
    }

    public static User getLoggedInUser(Message message) {
        UserAuthentication authentication = getLoggedInUserAuthentication(message);
        if (null == authentication ||
                !authentication.getPrincipal().getClass().isAssignableFrom(User.class)) {
            return null;
        }
        return authentication.getPrincipal();
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
                .parentCorrelationId(MessageContext.getCurrentContext().getCorrelationId())
                .authenticationLevel(AuthenticationLevel.CM_AUTHENTICATED)
                .identifier(authentication.getName())
                .identifierType(UserIdentifierType.USER_NICKNAME)
                .terminalCode(authentication.getTerminalCode())
//                .remoteAddress()
//                .xForwardedFor()
//                .hostAddress()
//                .instanceName()
                .build();
    }

}
