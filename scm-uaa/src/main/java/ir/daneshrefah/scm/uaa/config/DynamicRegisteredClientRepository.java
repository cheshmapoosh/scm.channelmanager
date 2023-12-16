package ir.daneshrefah.scm.uaa.config;

import ir.daneshrefah.scm.uaa.domain.AuthorizationGrantType;
import ir.daneshrefah.scm.uaa.domain.client.Client;
import ir.daneshrefah.scm.uaa.domain.client.ClientAuthenticationMethod;
import ir.daneshrefah.scm.uaa.service.ClientService;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class DynamicRegisteredClientRepository implements RegisteredClientRepository {

    private ClientService clientService;

    Map<ClientAuthenticationMethod, org.springframework.security.oauth2.core.ClientAuthenticationMethod> springAuthenticationMapping = new HashMap<>();
    Map<AuthorizationGrantType, org.springframework.security.oauth2.core.AuthorizationGrantType> springAuthorizationMapping = new HashMap<>();

    public DynamicRegisteredClientRepository(ClientService clientService) {
        this.clientService = clientService;
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

    @Override
    public void save(RegisteredClient registeredClient) {

    }

    @Override
    public RegisteredClient findById(String id) {
        return findAll().stream()
                .filter(registeredClient -> registeredClient.getId().equals(id))
                .findFirst().orElseThrow();
    }

    @Override
    public RegisteredClient findByClientId(String clientId) {
        return findAll().stream()
                .filter(registeredClient -> registeredClient.getClientId().equals(clientId))
                .findFirst().orElseThrow();
    }

    private List<RegisteredClient> findAll() {
        List<Client> clients = clientService.findAll();
        return clients.stream().map(client -> {
            RegisteredClient.Builder clientBuilder = RegisteredClient.withId(client.getId())
                    .clientId(client.getClientId())
                    .clientSecret(client.getClientSecret())
                    .clientAuthenticationMethod(springAuthenticationMapping.get(client.getAuthenticationMethod()))
                    .clientSettings(ClientSettings.builder().requireAuthorizationConsent(client.isRequireAuthorizationConsent()).build());
            for (Iterator<ir.daneshrefah.scm.uaa.domain.AuthorizationGrantType> iterator = client.getAuthorizationGrantTypes().iterator(); iterator.hasNext(); ) {
                ir.daneshrefah.scm.uaa.domain.AuthorizationGrantType authorizationGrantType = iterator.next();
                clientBuilder.authorizationGrantType(springAuthorizationMapping.get(authorizationGrantType));
            }
            for (Iterator<String> iterator = client.getRedirectUrls().iterator(); iterator.hasNext(); ) {
                String redirectUri = iterator.next();
                clientBuilder.redirectUri(redirectUri);
            }
            for (Iterator<String> iterator = client.getScopes().iterator(); iterator.hasNext(); ) {
                String scope = iterator.next();
                clientBuilder.scope(scope);
            }
            return clientBuilder.build();
        }).collect(Collectors.toList());
    }

}
