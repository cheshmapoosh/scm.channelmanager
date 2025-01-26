package ir.daneshrefah.scm.uaa.config;

import ir.daneshrefah.scm.uaa.common.utils.AuthenticationUtils;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component("uaaAuditorProvider")
public class AuditorAwareImpl implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        return Optional.ofNullable(AuthenticationUtils.getLoggedInGlobalUsername());
    }
}