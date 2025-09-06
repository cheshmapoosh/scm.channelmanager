package ir.daneshrefah.scm.core.authority.decision.manager;

import ir.daneshrefah.scm.common.exception.AuthenticationRequiredException;
import ir.daneshrefah.scm.common.model.gateway.BaseChannelServiceDefinition;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.core.authority.decision.constant.AuthorizationManagerChainDefinition;
import ir.daneshrefah.scm.core.authority.decision.manager.chain.DefaultAuthorizationManagerChain;
import ir.daneshrefah.scm.plugin.api.authority.exception.AuthorityBaseException;
import ir.daneshrefah.scm.uaa.common.utils.AuthenticationUtils;
import ir.daneshrefah.scm.utils.string.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.beans.Introspector;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class SecurityDecisionManagerImpl implements AuthorizationDecisionChainManager {

    private final Map<String, AuthorizationManagerDecisionChain> decisionChains;
    private final AuthorizationManagerFactory authorizationManagerFactory;
    private final Map<String,AuthorizationManager<Exchange>> authorities;


    @Override
    public void decide(Exchange exchange) throws AuthorityBaseException {
        Authentication authentication = AuthenticationUtils.getAuthentication();
        if (Objects.isNull(authentication)) {
            throw new AuthenticationRequiredException();
        }
        BaseChannelServiceDefinition baseChannelServiceDefinition = (BaseChannelServiceDefinition) exchange.getProperty(Message.CHANNEL_SERVICE_DEFINITION);
        BaseChannelServiceDefinition.AuthorizationConfig authorizationConfig = baseChannelServiceDefinition.getAuthorizationConfig();
        if (Objects.nonNull(authorizationConfig)) {
            String chain = authorizationConfig.getChain();
            chain = StringUtils.isBlank(chain) ? Introspector.decapitalize(DefaultAuthorizationManagerChain.class.getSimpleName()) : chain;
            AuthorizationManagerDecisionChain decisionChain = decisionChains.get(chain);
            if (Objects.isNull(decisionChain)) {
                throw new IllegalArgumentException("Could not found any AuthorizationManagerDecisionChain with name '" + chain + "'");
            }
            AuthorizationManagerChainDefinition chainDefinition = decisionChain.decisionChain().build();
            Optional<AuthorizationManager<AuthorizationData>> authorizationManager = authorizationManagerFactory.getAuthorizationManager(chainDefinition.getManagerBeanName());
            final List<Class<? extends AuthorizationManager<Exchange>>> authoritiesClassList = chainDefinition.getAuthorities();
            final List<AuthorizationManager<Exchange>> authorizationList = authoritiesClassList
                    .stream()
                    .map(ac-> authorities.get(Introspector.decapitalize(ac.getSimpleName())))
                    .toList();
            authorizationManager
                    .ifPresentOrElse(manager -> {
                        AuthorizationData authorizationData = new AuthorizationData(exchange, authorizationList);
                        manager.verify(() -> authentication, authorizationData);
                    }, () -> {
                        throw new IllegalArgumentException("Could not found any AuthorizationManager with name '" + chainDefinition.getManagerBeanName() + "'");
                    });
        }
    }
}
