package ir.daneshrefah.scm.uaa.client.session;

import ir.daneshrefah.scm.uaa.client.security.ScmPrincipal;

import java.util.Optional;

public interface ScmSessionReader {

    Optional<ScmSessionView> findCurrentSession(ScmPrincipal principal);
}
