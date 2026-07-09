package ir.daneshrefah.scm.uaa.starter.session;

import ir.daneshrefah.scm.uaa.starter.security.ScmPrincipal;

import java.util.Optional;

public interface ScmSessionReader {

    Optional<ScmSessionView> findCurrentSession(ScmPrincipal principal);
}
