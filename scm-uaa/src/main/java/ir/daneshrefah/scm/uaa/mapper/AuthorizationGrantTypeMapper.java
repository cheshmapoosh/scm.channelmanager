package ir.daneshrefah.scm.uaa.mapper;

import ir.daneshrefah.scm.uaa.domain.AuthorizationGrantType;

import java.util.HashMap;
import java.util.Map;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-18
 */
public class AuthorizationGrantTypeMapper {

    public static AuthorizationGrantTypeMapper INSTANCE = new AuthorizationGrantTypeMapper();

    Map<AuthorizationGrantType, org.springframework.security.oauth2.core.AuthorizationGrantType> springAuthorizationMapping = new HashMap<>();

    public AuthorizationGrantTypeMapper() {
        springAuthorizationMapping.put(AuthorizationGrantType.AUTHORIZATION_CODE,
                org.springframework.security.oauth2.core.AuthorizationGrantType.AUTHORIZATION_CODE);
        springAuthorizationMapping.put(AuthorizationGrantType.CLIENT_CREDENTIALS,
                org.springframework.security.oauth2.core.AuthorizationGrantType.CLIENT_CREDENTIALS);
        springAuthorizationMapping.put(AuthorizationGrantType.REFRESH_TOKEN,
                org.springframework.security.oauth2.core.AuthorizationGrantType.REFRESH_TOKEN);
        springAuthorizationMapping.put(AuthorizationGrantType.FIRST_PASSWORD,
                new org.springframework.security.oauth2.core.AuthorizationGrantType(AuthorizationGrantType.FIRST_PASSWORD.name()));
        springAuthorizationMapping.put(AuthorizationGrantType.SECOND_PASSWORD,
                new org.springframework.security.oauth2.core.AuthorizationGrantType(AuthorizationGrantType.SECOND_PASSWORD.name()));
    }

    public org.springframework.security.oauth2.core.AuthorizationGrantType toSpring(AuthorizationGrantType value) {
        if (null == value) {
            return null;
        }
        return springAuthorizationMapping.get(value);
    }
}
