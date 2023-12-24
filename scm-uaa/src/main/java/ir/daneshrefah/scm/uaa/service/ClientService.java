package ir.daneshrefah.scm.uaa.service;

import ir.daneshrefah.scm.uaa.common.core.AuthorizationGrantType;
import ir.daneshrefah.scm.uaa.domain.client.Client;
import ir.daneshrefah.scm.uaa.domain.client.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ClientService {


    public List<Client> findAll() {
        return Arrays.asList(prepareMockClient());
    }

    public Client findByClientId(String clientId) {
        return findAll().stream().filter(client -> clientId.equals(client.getClientId())).findFirst().orElseThrow();
    }

    private Client prepareMockClient() {
        Client client = new Client();
        client.setId(UUID.randomUUID().toString());
        client.setTitle("Internet Bank");
        client.setClientId("ib");
        client.setClientSecret("{noop}myClientSecretValue");
        client.setAuthenticationMethods((Arrays.asList(ClientAuthenticationMethod.CLIENT_SECRET_BASIC,
                ClientAuthenticationMethod.CLIENT_SECRET_POST)));
        client.setAuthorizationGrantTypes(Arrays.asList(AuthorizationGrantType.AUTHORIZATION_CODE,
                AuthorizationGrantType.REFRESH_TOKEN, AuthorizationGrantType.CLIENT_CREDENTIALS,
                AuthorizationGrantType.FIRST_PASSWORD, AuthorizationGrantType.SECOND_PASSWORD));
        client.setRedirectUrls(Arrays.asList("http://127.0.0.1:8080/login/oauth2/code/users-client-oidc",
                "http://127.0.0.1:8080/authorized"));
        client.setTerminalCode("IB");
        client.setScopes(Arrays.asList(OidcScopes.OPENID, OidcScopes.PROFILE));
        client.setRequireAuthorizationConsent(true);
        return client;
    }

}
