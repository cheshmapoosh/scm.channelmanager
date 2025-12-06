package ir.daneshrefah.scm.cache.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.cache.domain.dto.AuthenticationDetailTO;
import ir.daneshrefah.scm.cache.domain.dto.UserAuthenticationTO;
import ir.daneshrefah.scm.common.model.user.AuthenticationMethod;
import ir.daneshrefah.scm.uaa.common.model.authentication.UserAuthentication;
import ir.daneshrefah.scm.uaa.common.model.user.User;
import ir.daneshrefah.scm.utils.string.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UserAuthenticationMapperImpl implements UserAuthenticationMapper {
    private static final String JOIN_SIGN = "::";
    private final ObjectMapper objectMapper;

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
        return new UserAuthentication(details, user);
    }

    @Override
    public UserAuthenticationTO mapUserAuthenticationTO(UserAuthentication userAuthentication) {
        UserAuthenticationTO authenticationTO = new UserAuthenticationTO();
        UserAuthentication.AuthenticationDetail details = userAuthentication.getDetails();
        User principal = userAuthentication.getPrincipal();
        if (Objects.nonNull(details)) {
            if (Objects.nonNull(details.getExpiresAt())) {
                authenticationTO.setExpiresAt(details.getExpiresAt().toEpochMilli());
            }
            if (Objects.nonNull(details.getMaxIdle())) {
                authenticationTO.setMaxIdle((int) details.getMaxIdle().toSeconds());
            }
            if (Objects.nonNull(details.getIssuedAt())) {
                authenticationTO.setIssuedAt(details.getIssuedAt().toEpochMilli());
            }
            authenticationTO.setIssuer(details.getIssuer());
            String accessParameter = principal.getAccessParameters().stream()
                    .map(n -> ";" + n + ";")
                    .collect(Collectors.joining(","));
            authenticationTO.setLoginAccessParameter(accessParameter);
            authenticationTO.setSessionId(details.getSessionId());
        }
        if (Objects.nonNull(principal)) {
            AuthenticationMethod loginAuthenticationMethod = principal.getLoginAuthenticationMethod();
            authenticationTO.setLoginAuthenticationMethod(Objects.nonNull(loginAuthenticationMethod) ? loginAuthenticationMethod.name() : null);
            authenticationTO.setNickName(principal.getNickname());
            authenticationTO.setTerminalCode(principal.getTerminalCode());
        }
        return authenticationTO;
    }

    @Override
    @SneakyThrows
    public UserAuthenticationTO mapUserAuthenticationTO(String json) {
        json = StringUtils.cleanUpJsonCharacters(json);
        JsonNode jsonNode = objectMapper.readTree(json);
        UserAuthenticationTO authenticationTO = new UserAuthenticationTO();
        AuthenticationDetailTO authenticationDetailTo = objectMapper.readValue(String.valueOf(jsonNode.get("details")), AuthenticationDetailTO.class);
        if (Objects.nonNull(authenticationDetailTo.getExpiresAt())) {
            authenticationTO.setExpiresAt(authenticationDetailTo.getExpiresAt().toEpochMilli());
        }
        if (Objects.nonNull(authenticationDetailTo.getMaxIdle())) {
            authenticationTO.setMaxIdle((int) authenticationDetailTo.getMaxIdle().toSeconds());
        }
        if (Objects.nonNull(authenticationDetailTo.getIssuedAt())) {
            authenticationTO.setIssuedAt(authenticationDetailTo.getIssuedAt().toEpochMilli());
        }
        authenticationTO.setIssuer(authenticationDetailTo.getIssuer());
        authenticationTO.setLoginAccessParameter(authenticationDetailTo.getLoginAccessParameter());
        authenticationTO.setSessionId(authenticationDetailTo.getSessionId());
        JsonNode principal = jsonNode.get("principal");
        if (Objects.nonNull(principal)) {
            AuthenticationMethod loginAuthenticationMethod = objectMapper.readValue(String.valueOf(principal.get("loginAuthenticationMethod")), AuthenticationMethod.class);
            authenticationTO.setLoginAuthenticationMethod(Objects.nonNull(loginAuthenticationMethod) ? loginAuthenticationMethod.name() : null);
            authenticationTO.setNickName(objectMapper.readValue(String.valueOf(principal.get("nickname")), String.class));
            authenticationTO.setTerminalCode(objectMapper.readValue(String.valueOf(principal.get("terminalCode")), String.class));
        }
        return authenticationTO;
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
