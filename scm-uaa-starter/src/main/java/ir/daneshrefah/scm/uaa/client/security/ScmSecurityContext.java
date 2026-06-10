package ir.daneshrefah.scm.uaa.client.security;

import java.util.Optional;

public interface ScmSecurityContext {

    ScmPrincipal requirePrincipal();

    Optional<ScmPrincipal> currentPrincipal();
}
