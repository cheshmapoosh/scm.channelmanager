package ir.daneshrefah.scm.uaa.starter.security;

public interface ScmOwnedResource {

    String ownerSubject();

    String ownerSessionId();

    default String ownerTerminalCode() {
        return null;
    }
}
