package ir.daneshrefah.scm.core.authority.decision.manager;

import ir.daneshrefah.scm.core.authority.decision.builder.AuthorizationManagerDecisionChainBuilder;

public interface AuthorizationManagerDecisionChain {
    AuthorizationManagerDecisionChainBuilder decisionChain();
}
