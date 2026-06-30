package ir.daneshrefah.scm.uaa.security.oauth2.policy;

import ir.daneshrefah.scm.uaa.domain.client.ClientVersion;
import ir.daneshrefah.scm.uaa.exception.ActivationCodeRequiredException;
import ir.daneshrefah.scm.uaa.exception.ClientCodeRequiredException;
import ir.daneshrefah.scm.uaa.exception.ClientVersionRequiredException;
import ir.daneshrefah.scm.uaa.exception.InvalidClientSignatureException;
import ir.daneshrefah.scm.uaa.exception.InvalidClientVersionException;
import ir.daneshrefah.scm.uaa.security.token.PreAuthenticationToken;
import ir.daneshrefah.scm.uaa.service.client.ClientService;
import ir.daneshrefah.scm.utils.string.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static ir.daneshrefah.scm.uaa.common.utils.Constants.CLIENT_SETTING_KEY_CHECK_ACTIVATION;
import static ir.daneshrefah.scm.uaa.common.utils.Constants.CLIENT_SETTING_KEY_CHECK_VERSION;

@Component
@RequiredArgsConstructor
public class ClientVersionPolicy {
    private final ClientService clientService;

    public void validate(PreAuthenticationToken authentication) {
        RegisteredClient registeredClient = authentication.getRegisteredClient();
        if (Objects.isNull(registeredClient)) {
            throw new ClientCodeRequiredException();
        }
        boolean checkVersion = registeredClient.getClientSettings().getSetting(CLIENT_SETTING_KEY_CHECK_VERSION);
        boolean checkActivation = registeredClient.getClientSettings().getSetting(CLIENT_SETTING_KEY_CHECK_ACTIVATION);
        if (checkActivation && StringUtils.isEmpty(authentication.getActivationCode())) {
            throw new ActivationCodeRequiredException();
        }
        if (!checkVersion) {
            return;
        }

        String clientVersion = authentication.getClientVersion();
        if (StringUtils.isEmpty(clientVersion)) {
            throw new ClientVersionRequiredException();
        }
        String clientSignature = authentication.getClientSignature();
        List<ClientVersion> clientVersions = clientService.findByNickname(registeredClient.getClientId()).orElseThrow().getVersions();
        Optional<ClientVersion> matchedClientVersion = clientVersions.stream()
                .filter(version -> clientVersion.equals(version.getAppVersion()))
                .findFirst();
        if (matchedClientVersion.isEmpty()) {
            throw new InvalidClientVersionException(clientVersion);
        }
        if (StringUtils.isNotEmpty(matchedClientVersion.get().getSignature())
                && !matchedClientVersion.get().getSignature().equals(clientSignature)) {
            throw new InvalidClientSignatureException();
        }
    }
}
