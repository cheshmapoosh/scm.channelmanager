package ir.daneshrefah.scm.uaa.service;

import ir.daneshrefah.scm.uaa.common.model.authentication.UaaScopes;
import ir.daneshrefah.scm.uaa.security.token.GeneralAuthenticationToken;

public interface ScopeAware {
    void doScopeJob(GeneralAuthenticationToken token);
    boolean supports(UaaScopes scope);
}
