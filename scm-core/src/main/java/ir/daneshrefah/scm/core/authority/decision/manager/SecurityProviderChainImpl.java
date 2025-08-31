package ir.daneshrefah.scm.core.authority.decision.manager;

import ir.daneshrefah.scm.core.authority.decision.builder.SecurityProviderChainBuilder;
import ir.daneshrefah.scm.core.authority.decision.voter.AuthenticationSecurityProvider;
import ir.daneshrefah.scm.core.authority.decision.voter.AuthorizationSecurityProvider;
import ir.daneshrefah.scm.core.authority.decision.voter.TransactionAuthenticationSecurityProvider;
import org.springframework.stereotype.Component;

@Component
public class SecurityProviderChainImpl implements SecurityProviderChain {

    @Override
    public SecurityProviderChainBuilder securityProviderChain() {
        return SecurityProviderChainBuilder
                .create()
                .register(AuthenticationSecurityProvider.class)
                .register(AuthorizationSecurityProvider.class)
                .register(TransactionAuthenticationSecurityProvider.class);
    }
}
