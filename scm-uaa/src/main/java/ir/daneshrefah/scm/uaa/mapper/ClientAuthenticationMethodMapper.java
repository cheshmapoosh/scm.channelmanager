package ir.daneshrefah.scm.uaa.mapper;

import ir.daneshrefah.scm.uaa.domain.client.ClientAuthenticationMethod;

import java.util.HashMap;
import java.util.Map;

public class ClientAuthenticationMethodMapper {

    public static ClientAuthenticationMethodMapper INSTANCE = new ClientAuthenticationMethodMapper();

    Map<ClientAuthenticationMethod, org.springframework.security.oauth2.core.ClientAuthenticationMethod> springAuthenticationMapping = new HashMap<>();

    public ClientAuthenticationMethodMapper() {
        springAuthenticationMapping.put(ClientAuthenticationMethod.CLIENT_SECRET_BASIC,
                org.springframework.security.oauth2.core.ClientAuthenticationMethod.CLIENT_SECRET_BASIC);
        springAuthenticationMapping.put(ClientAuthenticationMethod.CLIENT_SECRET_POST,
                org.springframework.security.oauth2.core.ClientAuthenticationMethod.CLIENT_SECRET_POST);
        springAuthenticationMapping.put(ClientAuthenticationMethod.CLIENT_SECRET_JWT,
                org.springframework.security.oauth2.core.ClientAuthenticationMethod.CLIENT_SECRET_JWT);
        springAuthenticationMapping.put(ClientAuthenticationMethod.PRIVATE_KEY_JWT,
                org.springframework.security.oauth2.core.ClientAuthenticationMethod.PRIVATE_KEY_JWT);
        springAuthenticationMapping.put(ClientAuthenticationMethod.NONE,
                org.springframework.security.oauth2.core.ClientAuthenticationMethod.NONE);
    }

    public org.springframework.security.oauth2.core.ClientAuthenticationMethod toSpring(ClientAuthenticationMethod value) {
        if (null == value) {
            return null;
        }
        return springAuthenticationMapping.get(value);
    }
}
