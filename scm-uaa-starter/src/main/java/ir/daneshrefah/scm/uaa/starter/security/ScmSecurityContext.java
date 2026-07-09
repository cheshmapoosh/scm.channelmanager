package ir.daneshrefah.scm.uaa.starter.security;

import java.util.Optional;

public interface ScmSecurityContext {

    ScmPrincipal requirePrincipal();

    Optional<ScmPrincipal> currentPrincipal();
}
