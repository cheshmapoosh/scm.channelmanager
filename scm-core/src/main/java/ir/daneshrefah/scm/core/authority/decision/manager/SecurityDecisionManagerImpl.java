package ir.daneshrefah.scm.core.authority.decision.manager;

import ir.daneshrefah.scm.core.authority.decision.constant.Vote;
import ir.daneshrefah.scm.core.authority.decision.voter.SecurityProvider;
import ir.daneshrefah.scm.plugin.api.authority.exception.AuthorityBaseException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
@RequiredArgsConstructor
@Slf4j
public class SecurityDecisionManagerImpl implements SecurityDecisionManager {

    private static final List<SecurityProvider> ORDERED_SECURITY_PROVIDER = new ArrayList<>();
    private final List<SecurityProviderChain> securityProviderChains;
    private final Map<Class<?>, SecurityProvider> securityProviders;

    @PostConstruct
    public void init() {
        if (Objects.isNull(securityProviderChains)) {
            throw new IllegalArgumentException("securityProviderChains could not found any implementation");
        }
        if (securityProviderChains.size() > 1) {
            throw new IllegalArgumentException("securityProviderChains must have only one implementation");
        }
        createOrderedSecurityProviderList();
        checkUnRegisteredSecurityProvider();
    }

    private void checkUnRegisteredSecurityProvider() {
        securityProviders
                .keySet()
                .stream()
                .filter(key -> !ORDERED_SECURITY_PROVIDER.contains(securityProviders.get(key)))
                .forEach(key -> log.warn(">>> [ALERT] SecurityProvider '{}' has not been registered on Chain.", key.getName()));
    }

    private void createOrderedSecurityProviderList() {
        securityProviderChains
                .get(0)
                .securityProviderChain()
                .build()
                .stream()
                .map(securityProviders::get)
                .peek(securityProvider -> log.info(">>> SecurityProvider '{}' has been registered", securityProvider.getClass().getName()))
                .forEachOrdered(ORDERED_SECURITY_PROVIDER::add);
    }

    @Override
    public void decide(Exchange exchange) throws AuthorityBaseException {
        ORDERED_SECURITY_PROVIDER.stream()
                .map(p -> p.getVote(exchange))
                .filter(vote -> Vote.ACCESS_ABSTAIN != vote)
                .findFirst()
                .ifPresent(vote -> {
                    if (vote == Vote.ACCESS_DENIED) {
                        throw new AuthorityBaseException();
                    }
                });
    }
}
