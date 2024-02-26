package ir.daneshrefah.scm.cache.mapper;

import ir.daneshrefah.scm.cache.domain.dto.UserAuthenticationTO;
import ir.daneshrefah.scm.uaa.common.model.authentication.UserAuthentication;
import ir.daneshrefah.scm.uaa.common.model.user.User;
import ir.daneshrefah.scm.uaa.common.type.AuthenticationMethod;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

@Component
public class UserAuthenticationMapperImpl implements UserAuthenticationMapper {
    private static final String JOIN_SIGN = "::";

    @Override
    public UserAuthentication mapToUserAuthentication(UserAuthenticationTO userAuthenticationTO) {
        UserAuthentication.AuthenticationDetail details =
        UserAuthentication
                .AuthenticationDetail
                .builder()
                .expiresAt(Instant.ofEpochMilli(userAuthenticationTO.getExpiresAt()))
                .issuedAt(Instant.ofEpochMilli(userAuthenticationTO.getIssuedAt()))
                .maxIdle(Duration.ofSeconds(userAuthenticationTO.getMaxIdle()))
                .loginAccessParameter(userAuthenticationTO.getLoginAccessParameter())
                .sessionId(userAuthenticationTO.getSessionId())
                .issuer(userAuthenticationTO.getIssuer())
                .build();
        User user = new User();
        user.setTerminalCode(userAuthenticationTO.getTerminalCode());
        user.setNickname(userAuthenticationTO.getNickName());
        if (Objects.nonNull(userAuthenticationTO.getLoginAuthenticationMethod())) {
            user.setLoginAuthenticationMethod(AuthenticationMethod.findByName(userAuthenticationTO.getLoginAuthenticationMethod().toUpperCase()));
        }
        return new UserAuthentication(details,user);
    }

    @Override
    public UserAuthenticationTO mapUserAuthenticationTO(UserAuthentication userAuthentication) {
        UserAuthenticationTO authenticationTO = new UserAuthenticationTO();
        authenticationTO.setExpiresAt(userAuthentication.getDetails().getExpiresAt().toEpochMilli());
        authenticationTO.setMaxIdle((int) userAuthentication.getDetails().getMaxIdle().toSeconds());
        authenticationTO.setIssuedAt(userAuthentication.getDetails().getIssuedAt().toEpochMilli());
        authenticationTO.setIssuer(userAuthentication.getDetails().getIssuer());
        authenticationTO.setLoginAccessParameter(userAuthentication.getDetails().getLoginAccessParameter());
        authenticationTO.setSessionId(userAuthentication.getDetails().getSessionId());
        User principal = userAuthentication.getPrincipal();
        if (Objects.nonNull(principal)) {
            AuthenticationMethod loginAuthenticationMethod = principal.getLoginAuthenticationMethod();
            authenticationTO.setLoginAuthenticationMethod(Objects.nonNull(loginAuthenticationMethod) ? loginAuthenticationMethod.name() : null);
            authenticationTO.setNickName(principal.getNickname());
            authenticationTO.setTerminalCode(principal.getTerminalCode());
        }
        return authenticationTO;
    }

    @Override
    public String generateKey(UserAuthentication userAuthentication) {
        User user = userAuthentication.getPrincipal();
        return user.getNickname() + JOIN_SIGN + user.getTerminalCode();
    }

    @Override
    public String generateKey(UserAuthenticationTO authenticationTO) {
        return authenticationTO.getNickName() + JOIN_SIGN + authenticationTO.getTerminalCode();
    }

    @Override
    public String generateKey(String nickName, String terminalCode) {
        return nickName + JOIN_SIGN + terminalCode;
    }
}
