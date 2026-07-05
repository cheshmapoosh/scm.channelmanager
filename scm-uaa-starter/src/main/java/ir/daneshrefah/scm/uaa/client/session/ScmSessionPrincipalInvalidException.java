package ir.daneshrefah.scm.uaa.client.session;

public class ScmSessionPrincipalInvalidException extends RuntimeException {

    public ScmSessionPrincipalInvalidException() {
        super("Current principal is missing required session identity claims");
    }
}
