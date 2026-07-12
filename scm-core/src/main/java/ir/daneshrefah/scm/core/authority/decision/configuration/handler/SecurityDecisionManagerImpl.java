package ir.daneshrefah.scm.core.authority.decision.configuration.handler;

import ir.daneshrefah.scm.common.exception.AuthenticationRequiredException;
import ir.daneshrefah.scm.common.model.gateway.BaseChannelServiceDefinition;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.core.authority.decision.chains.DefaultAuthorizationManagerChain;
import ir.daneshrefah.scm.core.authority.decision.configuration.model.AuthoritiesSecurityContext;
import ir.daneshrefah.scm.core.authority.decision.configuration.model.AuthorizationManagerChainDefinition;
import ir.daneshrefah.scm.core.authority.decision.configuration.model.SecurityContext;
import ir.daneshrefah.scm.core.integration.security.ExchangeAuthenticationContext;
import ir.daneshrefah.scm.plugin.api.authority.exception.AuthorityBaseException;
import ir.daneshrefah.scm.utils.string.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.beans.Introspector;
import java.util.ArrayList;
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
    private final BeanFactory beanFactory;


    @Override
    public void decide(Exchange exchange) throws AuthorityBaseException {
        Authentication authentication = ExchangeAuthenticationContext.authentication(exchange);
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
            Optional<AuthorizationManager<AuthoritiesSecurityContext>> authorizationManager = authorizationManagerFactory.getAuthorizationManager(chainDefinition.getManagerBeanName());
            final List<Class<? extends AuthorizationManager<SecurityContext>>> authoritiesClassList = chainDefinition.getAuthorities();
            if (authoritiesClassList.isEmpty() && chainDefinition.isDynamicAuthorization()) {
                registerAuthorizationClassDynamically(authoritiesClassList, exchange);
            }
            final List<AuthorizationManager<SecurityContext>> authorizationList = authoritiesClassList
                    .stream()
                    .map(ac -> authorizationManagerFactory.getAuthorizationVoter(Introspector.decapitalize(ac.getSimpleName())))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .toList();
            authorizationManager
                    .ifPresentOrElse(manager -> {
                        AuthoritiesSecurityContext authoritiesSecurityContext = new AuthoritiesSecurityContext(authorizationList);
                        authoritiesSecurityContext.setExchange(exchange);
                        authoritiesSecurityContext.setServiceAcceptableRoles(getAllServiceAcceptableRoles(exchange));
                        authoritiesSecurityContext.setUserRoles(getAllUserRoles(exchange));
                        manager.verify(() -> authentication, authoritiesSecurityContext);
                    }, () -> {
                        throw new IllegalArgumentException("Could not found any AuthorizationManager with name '" + chainDefinition.getManagerBeanName() + "'");
                    });
        }
    }

    private List<String> getAllUserRoles(Exchange exchange) {
        return ExchangeAuthenticationContext.jwtBusinessContext(exchange).roles();
    }

    private List<String> getAllServiceAcceptableRoles(Exchange exchange) {
        BaseChannelServiceDefinition baseChannelServiceDefinition = (BaseChannelServiceDefinition) exchange.getProperty(Message.CHANNEL_SERVICE_DEFINITION);
        return Optional
                .ofNullable(baseChannelServiceDefinition)
                .map(BaseChannelServiceDefinition::getAuthorizationConfig)
                .map(BaseChannelServiceDefinition.AuthorizationConfig::getAccessRoles)
                .orElse(new ArrayList<>());
    }

    @SuppressWarnings("unchecked")
    private void registerAuthorizationClassDynamically(List<Class<? extends AuthorizationManager<SecurityContext>>> authoritiesClassList, Exchange exchange) {
        BaseChannelServiceDefinition baseChannelServiceDefinition = (BaseChannelServiceDefinition) exchange.getProperty(Message.CHANNEL_SERVICE_DEFINITION);
        BaseChannelServiceDefinition.AuthorizationConfig authorizationConfig = baseChannelServiceDefinition.getAuthorizationConfig();
        List<String> authorities = authorizationConfig.getAuthorities();
        if (Objects.nonNull(authorities) && !authorities.isEmpty()) {
            authorities
                    .stream()
                    .map(bean -> beanFactory.getBean(bean).getClass()).forEach(aClass -> {
                        authoritiesClassList.add((Class<? extends AuthorizationManager<SecurityContext>>) aClass);
                    });
        }
    }


}
