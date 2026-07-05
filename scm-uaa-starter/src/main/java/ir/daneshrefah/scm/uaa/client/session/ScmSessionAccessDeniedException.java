package ir.daneshrefah.scm.uaa.client.session;

public class ScmSessionAccessDeniedException extends RuntimeException {

    public ScmSessionAccessDeniedException() {
        super("Current principal is not allowed to access the cached session");
    }
}
