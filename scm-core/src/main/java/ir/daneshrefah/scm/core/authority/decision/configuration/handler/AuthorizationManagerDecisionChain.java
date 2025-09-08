package ir.daneshrefah.scm.core.authority.decision.configuration.handler;

import ir.daneshrefah.scm.core.authority.decision.configuration.builder.AuthorizationManagerDecisionChainBuilder;

public interface AuthorizationManagerDecisionChain {
    AuthorizationManagerDecisionChainBuilder decisionChain();
}
