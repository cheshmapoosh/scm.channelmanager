package ir.daneshrefah.scm.uaa.service;

import ir.daneshrefah.scm.uaa.common.core.AuthorizationGrantType;
import ir.daneshrefah.scm.uaa.domain.client.Client;
import ir.daneshrefah.scm.uaa.domain.client.ClientAuthenticationMethod;
import ir.daneshrefah.scm.uaa.domain.client.ClientVersion;
import ir.daneshrefah.scm.uaa.domain.client.ClientVersionStatus;
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
        client.setTitle("Mobile Bank");
        client.setClientId("ib");
        client.setClientSecret("{noop}myClientSecretValue");
        client.setAuthenticationMethods((Arrays.asList(ClientAuthenticationMethod.CLIENT_SECRET_BASIC,
                ClientAuthenticationMethod.CLIENT_SECRET_POST)));
        client.setAuthorizationGrantTypes(Arrays.asList(AuthorizationGrantType.AUTHORIZATION_CODE,
                 /*AuthorizationGrantType.REFRESH_TOKEN, */AuthorizationGrantType.CLIENT_CREDENTIALS,
                AuthorizationGrantType.FIRST_PASSWORD, AuthorizationGrantType.SECOND_PASSWORD));
        client.setRedirectUrls(Arrays.asList("http://127.0.0.1:8080/login/oauth2/code/users-client-oidc",
                "http://127.0.0.1:8080/authorized"));
        client.setTerminalCode("IB");
        client.setScopes(Arrays.asList(OidcScopes.OPENID, OidcScopes.PROFILE));
        client.setRequireAuthorizationConsent(true);
        client.setRequireClientAuthentication(false);
        client.setCheckVersion(true);
        client.setCheckActivation(true);
        client.setSessionTimeToLive(10L);
        client.setVersions(Arrays.asList(
                new ClientVersion("MB-3.3.4",
                        "DF2A4EB3A644FE1F43DFBD9D818991B8262AD45982D5A9BD81A1D5CDB0EA0A0A132ADF9AC3097E07734942817A0A6CE32155F106C6D613999412A266B0A6B0A4",
                        ClientVersionStatus.VALID, "https://newmob.rkbank.ir")));
        return client;
    }

}
