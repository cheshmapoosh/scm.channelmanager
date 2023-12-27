package ir.daneshrefah.scm.uaa.security;

import ir.daneshrefah.scm.uaa.common.model.authentication.UaaScopes;
import ir.daneshrefah.scm.uaa.security.token.GeneralAuthenticationToken;
import ir.daneshrefah.scm.uaa.service.ScopeAware;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
@AllArgsConstructor
public class OAuthScopeHandler {
    private final List<ScopeAware> scopeAwareProviders;

    public void doJobs(GeneralAuthenticationToken token, Set<String> scopes) {
        scopes.stream().forEach(s -> {
            UaaScopes byScopeCode = UaaScopes.findByScopeCode(s);
            scopeAwareProviders.stream()
                    .filter(p->p.supports(byScopeCode))
                    .findFirst()
                    .orElseThrow()
                    .doScopeJob(token);
        });
    }
}
