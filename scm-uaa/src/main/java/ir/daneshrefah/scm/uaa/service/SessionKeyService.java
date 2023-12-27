package ir.daneshrefah.scm.uaa.service;

import ir.daneshrefah.scm.uaa.common.model.authentication.UaaScopes;
import ir.daneshrefah.scm.uaa.security.token.GeneralAuthenticationToken;
import ir.daneshrefah.scm.uaa.security.token.PostAuthenticationToken;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

@Service
public class SessionKeyService implements ScopeAware{
    private final static Logger logger= LoggerFactory.getLogger(SessionKeyService.class);
    private final static String SEPARATOR="::";

    private static String generateKey(String username,String delegator,String client)
    {
        logger.info("Generating sessionKey");
        boolean delegatorNotEmpty = StringUtils.isNotEmpty(delegator);
        String mainUser= delegatorNotEmpty?delegator:username;
        String secondaryUser=delegatorNotEmpty?username:null;
        String plainSession =mainUser
                .concat(SEPARATOR)
                .concat(Optional.ofNullable(secondaryUser).orElse("null"))
                .concat(SEPARATOR)
                .concat(client)
                .concat(SEPARATOR)
                .concat(UUID.randomUUID().toString());
        byte[] encodedBytes = Base64.getEncoder().encode(plainSession.getBytes());
        String encodedSession = new String(encodedBytes);
        return encodedSession;
    }

    public static void setSessionKeyForToken(GeneralAuthenticationToken generalAuthenticationToken)
    {

        String sessionKey = generateKey(generalAuthenticationToken.getName(),
                null,
                String.valueOf(generalAuthenticationToken.getPreAuthenticationToken().getClientPrincipal().getPrincipal()));
        if (PostAuthenticationToken.class.isAssignableFrom(generalAuthenticationToken.getClass()))
            ((PostAuthenticationToken) generalAuthenticationToken).setSessionKey(sessionKey);
    }

    public static SessionKeyModel decodeSessionKey(String sessionKey)
    {
        logger.info("Decoding sessionKey");
        byte[] decodedBytes = Base64.getDecoder().decode(sessionKey.getBytes(StandardCharsets.UTF_8));
        String decoded=new String(decodedBytes);
        String[] split = decoded.split(SEPARATOR);
        SessionKeyModel model= SessionKeyModel.builder()
                .username(split[0])
                .delegatedUser(split[1])
                .client(split[2])
                .uuid(split[3])
                .build();
        return model;

    }

    public static String getSessionRelatedCacheName(String sessionKey) {
        SessionKeyModel sessionKeyModel=decodeSessionKey(sessionKey);
        String cacheKey =sessionKeyModel.getUsername()
                .concat(SEPARATOR)
                .concat(sessionKeyModel.getDelegatedUser())
                .concat(SEPARATOR)
                .concat(sessionKeyModel.getClient());
        return cacheKey;
    }

    @Override
    public void doScopeJob(GeneralAuthenticationToken token) {
        setSessionKeyForToken(token);
    }

    @Override
    public boolean supports(UaaScopes scope) {
        return UaaScopes.SESSION_KEY_SCOPE.equals(scope);
    }

    @Getter
    @Setter
    @Builder
    public static class SessionKeyModel{
        private String username;
        private String delegatedUser;
        private String client;
        private String uuid;
    }
}
