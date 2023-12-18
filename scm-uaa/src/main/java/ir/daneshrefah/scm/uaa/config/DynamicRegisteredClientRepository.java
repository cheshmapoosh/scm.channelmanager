package ir.daneshrefah.scm.uaa.config;

import ir.daneshrefah.scm.uaa.domain.AuthorizationGrantType;
import ir.daneshrefah.scm.uaa.domain.client.Client;
import ir.daneshrefah.scm.uaa.domain.client.ClientAuthenticationMethod;
import ir.daneshrefah.scm.uaa.mapper.AuthorizationGrantTypeMapper;
import ir.daneshrefah.scm.uaa.mapper.ClientAuthenticationMethodMapper;
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

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-08-06
 */
@Component
public class DynamicRegisteredClientRepository implements RegisteredClientRepository {

    private ClientService clientService;
    private List<Client> clients = null;


    public DynamicRegisteredClientRepository(ClientService clientService) {
        this.clientService = clientService;
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
        if (null == clients) {
            clients = clientService.findAll();
        }

        return clients.stream().map(client -> {
            RegisteredClient.Builder clientBuilder = RegisteredClient.withId(client.getId())
                    .clientId(client.getClientId())
                    .clientSecret(client.getClientSecret())
                    .clientAuthenticationMethod(ClientAuthenticationMethodMapper.INSTANCE.toSpring(client.getAuthenticationMethod()))
                    .clientSettings(ClientSettings.builder().requireAuthorizationConsent(client.isRequireAuthorizationConsent()).build());
            for (Iterator<ir.daneshrefah.scm.uaa.domain.AuthorizationGrantType> iterator = client.getAuthorizationGrantTypes().iterator(); iterator.hasNext(); ) {
                ir.daneshrefah.scm.uaa.domain.AuthorizationGrantType authorizationGrantType = iterator.next();
                clientBuilder.authorizationGrantType(AuthorizationGrantTypeMapper.INSTANCE.toSpring(authorizationGrantType));
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
