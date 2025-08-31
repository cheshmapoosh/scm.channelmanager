package ir.daneshrefah.scm.core.authority.decision.manager;

import ir.daneshrefah.scm.core.authority.decision.builder.SecurityProviderChainBuilder;

public interface SecurityProviderChain {
    SecurityProviderChainBuilder securityProviderChain();
}
