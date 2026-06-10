package ir.daneshrefah.scm.uaa.client.security;

public interface ScmOwnedResource {

    String ownerSubject();

    String ownerSessionId();

    default String ownerTerminalCode() {
        return null;
    }
}
