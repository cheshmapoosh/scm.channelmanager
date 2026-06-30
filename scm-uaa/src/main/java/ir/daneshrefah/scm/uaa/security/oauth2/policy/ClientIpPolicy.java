package ir.daneshrefah.scm.uaa.security.oauth2.policy;

import ir.daneshrefah.scm.uaa.common.utils.AuthenticationUtils;
import ir.daneshrefah.scm.uaa.exception.ClientIpAddressNotAllowedException;
import ir.daneshrefah.scm.uaa.security.authentication.token.PreAuthenticationToken;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Set;

import static ir.daneshrefah.scm.uaa.common.utils.Constants.CLIENT_SETTING_KEY_ALLOW_IP_ADDRESSES;
import static ir.daneshrefah.scm.uaa.common.utils.Constants.CLIENT_SETTING_KEY_CHECK_IP_ADDRESS;

@Component
public class ClientIpPolicy {
    public void validate(PreAuthenticationToken authentication) {
        RegisteredClient registeredClient = authentication.getRegisteredClient();
        if (Objects.isNull(registeredClient)) {
            throw new ClientIpAddressNotAllowedException("registeredClient is null");
        }
        boolean checkIpAddress = registeredClient.getClientSettings().getSetting(CLIENT_SETTING_KEY_CHECK_IP_ADDRESS);
        if (!checkIpAddress) {
            return;
        }

        Set<String> allowIpAddresses = registeredClient.getClientSettings().getSetting(CLIENT_SETTING_KEY_ALLOW_IP_ADDRESSES);
        if (CollectionUtils.isEmpty(allowIpAddresses)) {
            throw new ClientIpAddressNotAllowedException("allowIpAddresses is empty");
        }

        boolean match = allowIpAddresses.stream()
                .anyMatch(ipAddress -> AuthenticationUtils.isIpAddressMatches(ipAddress, authentication.getRemoteAddress()));
        if (!match) {
            throw new ClientIpAddressNotAllowedException();
        }
    }
}
