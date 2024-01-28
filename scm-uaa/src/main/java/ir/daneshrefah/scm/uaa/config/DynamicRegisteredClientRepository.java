package ir.daneshrefah.scm.uaa.config;

import ir.daneshrefah.scm.uaa.common.core.AuthorizationGrantType;
import ir.daneshrefah.scm.uaa.domain.client.Client;
import ir.daneshrefah.scm.uaa.domain.client.ClientAuthenticationMethod;
import ir.daneshrefah.scm.uaa.mapper.AuthorizationGrantTypeMapper;
import ir.daneshrefah.scm.uaa.mapper.ClientAuthenticationMethodMapper;
import ir.daneshrefah.scm.uaa.service.ClientService;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import static ir.daneshrefah.scm.uaa.common.utils.Constants.CLIENT_SETTING_KEY_TERMINAL_CODE;


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
                .filter(registeredClient -> registeredClient.getClientId().equalsIgnoreCase(clientId))
                .findFirst().orElse(null);
    }

    private List<RegisteredClient> findAll() {
        if (null == clients) {
            clients = clientService.findAll();
        }

        return clients.stream().map(client -> {
            Duration accessTokenTimeToLive = null != client.getSessionTimeToLive() ?
                    Duration.ofMinutes(client.getSessionTimeToLive()) : Duration.ofMinutes(5);
            TokenSettings tokenSettings = TokenSettings.builder()
                    .accessTokenTimeToLive(accessTokenTimeToLive)
                    .build();
            ClientSettings clientSetting = ClientSettings.builder()
                    .requireAuthorizationConsent(client.isRequireAuthorizationConsent())
                    .setting(CLIENT_SETTING_KEY_TERMINAL_CODE, client.getTerminalCode())
                    .build();
            RegisteredClient.Builder clientBuilder = RegisteredClient.withId(String.valueOf(client.getId()))
                    .clientId(client.getClientId())
                    .clientSecret(client.getClientSecret())
//                    .clientAuthenticationMethod(ClientAuthenticationMethodMapper.INSTANCE.toSpring(client.getAuthenticationMethod()))
                    .tokenSettings(tokenSettings)
                    .clientSettings(clientSetting);
            for (Iterator<ClientAuthenticationMethod> iterator = client.getAuthenticationMethods().iterator(); iterator.hasNext(); ) {
                ClientAuthenticationMethod clientAuthenticationMethod = iterator.next();
                clientBuilder.clientAuthenticationMethod(ClientAuthenticationMethodMapper.INSTANCE.toSpring(clientAuthenticationMethod));
            }
            for (Iterator<AuthorizationGrantType> iterator = client.getAuthorizationGrantTypes().iterator(); iterator.hasNext(); ) {
                AuthorizationGrantType authorizationGrantType = iterator.next();
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
