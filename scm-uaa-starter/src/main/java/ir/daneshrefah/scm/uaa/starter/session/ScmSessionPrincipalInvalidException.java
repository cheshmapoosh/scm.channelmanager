package ir.daneshrefah.scm.uaa.starter.session;

public class ScmSessionPrincipalInvalidException extends RuntimeException {

    public ScmSessionPrincipalInvalidException() {
        super("Current principal is missing required session identity claims");
    }
}
