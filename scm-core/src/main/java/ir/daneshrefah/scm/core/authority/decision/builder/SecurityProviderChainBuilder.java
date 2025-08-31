package ir.daneshrefah.scm.core.authority.decision.builder;

import ir.daneshrefah.scm.core.authority.decision.voter.SecurityProvider;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SecurityProviderChainBuilder {

    private final List<Class<? extends SecurityProvider>> securityProviderList = new ArrayList<>();

    public static SecurityProviderChainBuilder create() {
        return new SecurityProviderChainBuilder();
    }

    public SecurityProviderChainBuilder register(Class<? extends SecurityProvider> securityProvider) {
        this.securityProviderList.add(securityProvider);
        return this;
    }

    public List<Class<? extends SecurityProvider>> build() {
        return securityProviderList;
    }
}
