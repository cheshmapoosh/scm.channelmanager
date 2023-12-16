package ir.daneshrefah.scm.uaa.service;

import ir.daneshrefah.scm.uaa.domain.AuthorizationGrantType;
import ir.daneshrefah.scm.uaa.domain.client.Client;
import ir.daneshrefah.scm.uaa.domain.client.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class ClientService {

    public List<Client> findAll() {
        return Arrays.asList(prepareMockClient());
    }

    private Client prepareMockClient() {
        Client client = new Client();
        client.setId(UUID.randomUUID().toString());
        client.setClientId("client1");
        client.setClientSecret("{noop}myClientSecretValue");
        client.setAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC);
        client.setAuthorizationGrantTypes(Arrays.asList(AuthorizationGrantType.AUTHORIZATION_CODE,
                AuthorizationGrantType.REFRESH_TOKEN));
        client.setRedirectUrls(Arrays.asList("http://127.0.0.1:8080/login/oauth2/code/users-client-oidc",
                "http://127.0.0.1:8080/authorized"));
        client.setTerminalScope("IB");
        client.setScopes(Arrays.asList(OidcScopes.OPENID, OidcScopes.PROFILE));
        client.setRequireAuthorizationConsent(true);
        return client;
    }

}
