package ir.daneshrefah.scm.uaa.config;

import ir.daneshrefah.scm.uaa.common.core.AuthorizationGrantType;
import ir.daneshrefah.scm.uaa.domain.client.Client;
import ir.daneshrefah.scm.uaa.domain.client.ClientAuthenticationMethod;
import ir.daneshrefah.scm.uaa.domain.client.ClientAuthorizationGrantType;
import ir.daneshrefah.scm.uaa.domain.client.ClientScopeRelation;
import ir.daneshrefah.scm.uaa.mapper.AuthorizationGrantTypeMapper;
import ir.daneshrefah.scm.uaa.mapper.ClientAuthenticationMethodMapper;
import ir.daneshrefah.scm.uaa.service.client.ClientService;
import ir.daneshrefah.scm.utils.string.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Objects;

import static ir.daneshrefah.scm.uaa.common.utils.Constants.*;


/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-08-06
 */
@Slf4j
@Component
public class DynamicRegisteredClientRepository implements RegisteredClientRepository {

    private final ClientService clientService;
    private List<Client> clients = null;


    public DynamicRegisteredClientRepository(ClientService clientService) {
        this.clientService = clientService;
    }

    @Override
    public void save(RegisteredClient registeredClient) {

    }

    @Override
    public RegisteredClient findById(String id) {
        return findAll()
                .stream()
                .filter(client -> String.valueOf(client.getId()).equals(id))
                .map(this::mapToRegisteredClient)
                .findFirst()
                .orElseGet(() -> {
                    log.warn(">>> the client with id : {} dos not found", id);
                    return null;
                });
    }

    @Override
    public RegisteredClient findByClientId(String nickname) {
        return findAll()
                .stream()
                .filter(client -> client.getUser().getNickname().equalsIgnoreCase(nickname))
                .map(this::mapToRegisteredClient)
                .findFirst().orElseGet(() -> {
                    log.warn(">>> the client with clientId : {} dos not found", nickname);
                    return null;
                });
    }


    private RegisteredClient mapToRegisteredClient(Client client) {
        Duration accessTokenTimeToLive = null != client.getSessionTimeToLiveMinute() ?
                Duration.ofMinutes(client.getSessionTimeToLiveMinute()) : Duration.ofMinutes(5);
        TokenSettings tokenSettings = TokenSettings.builder()
                .accessTokenTimeToLive(accessTokenTimeToLive)
                .refreshTokenTimeToLive(accessTokenTimeToLive)
                .build();
        ClientSettings clientSetting = ClientSettings.builder()
                .requireAuthorizationConsent(client.isRequireAuthorizationConsent())
                .setting(CLIENT_SETTING_KEY_TERMINAL_CODE, client.getTerminalCode())
                .setting(CLIENT_SETTING_KEY_CHECK_VERSION, client.isCheckVersion())
                .setting(CLIENT_SETTING_KEY_CHECK_ACTIVATION, client.isCheckActivation())
                .setting(CLIENT_SETTING_KEY_CHECK_IP_ADDRESS, client.isCheckIpAddress())
                .setting(CLIENT_SETTING_KEY_ALLOW_IP_ADDRESSES, client.getAllowIpAddresses())
                .build();
        RegisteredClient.Builder clientBuilder = RegisteredClient.withId(String.valueOf(client.getId()))
                .clientId(client.getUser().getNickname())
                .clientSecret(Objects.nonNull(client.getUser()) && StringUtils.isNotBlank(client.getUser().getLoginStaticPassword()) ? client.getUser().getLoginStaticPassword() : "{noop}myClientSecretValue")
//                    .clientAuthenticationMethod(ClientAuthenticationMethodMapper.INSTANCE.toSpring(client.getAuthenticationMethod()))
                .tokenSettings(tokenSettings)
                .clientSettings(clientSetting);
        for (ClientAuthenticationMethod clientAuthenticationMethod : client.getAuthenticationMethods()) {
            clientBuilder.clientAuthenticationMethod(ClientAuthenticationMethodMapper.INSTANCE.toSpring(clientAuthenticationMethod));
        }
        List<AuthorizationGrantType> grantTypes = client
                .getClientAuthorizationGrantTypes()
                .stream()
                .map(ClientAuthorizationGrantType::getAuthorizationGrantType)
                .toList();
        if (grantTypes.isEmpty()) {
            log.warn(">>> important! the client with nickname : {} does not have any authorizationGrantType", client.getUser().getNickname());
        } else {
            grantTypes
                    .stream()
                    .map(AuthorizationGrantTypeMapper.INSTANCE::toSpring)
                    .forEach(clientBuilder::authorizationGrantType);
        }

        for (String redirectUri : client.getRedirectUris()) {
            clientBuilder.redirectUri(redirectUri);
        }
        boolean isScopeOpenIdAdded = false;
        if (null != client.getScopes()) {
            for (ClientScopeRelation scope : client.getScopes()) {
                clientBuilder.scope(scope.getScope().getCode());
                isScopeOpenIdAdded = isScopeOpenIdAdded || OidcScopes.OPENID.equalsIgnoreCase(scope.getScope().getCode());
            }
        }
        if (!isScopeOpenIdAdded) {
            clientBuilder.scope(OidcScopes.OPENID);
        }
        //TODO: Resolve bug for fetch from db
        clientBuilder.scope("session");
        return clientBuilder.build();
    }

    private List<Client> findAll() {
        if (null == clients) {
            clients = clientService.findAll();
        }
        return clients;
    }

}
