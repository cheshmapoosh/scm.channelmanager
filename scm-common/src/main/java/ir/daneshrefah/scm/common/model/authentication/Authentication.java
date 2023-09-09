package ir.daneshrefah.scm.common.model.authentication;

import ir.daneshrefah.scm.common.model.authority.Authority;
import ir.daneshrefah.scm.common.model.terminal.Terminal;

import java.io.Serializable;
import java.util.List;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-08-14
 */
public class Authentication implements Serializable {

    private String sessionKey;
    private String username;
    private String accessParameter;
    private Terminal terminal;
    private List<Authority> authorities;

    public Authentication(String username) {
        this.username = username;
    }

    public String getSessionKey() {
        return sessionKey;
    }

    public void setSessionKey(String sessionKey) {
        this.sessionKey = sessionKey;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAccessParameter() {
        return accessParameter;
    }

    public void setAccessParameter(String accessParameter) {
        this.accessParameter = accessParameter;
    }

    public Terminal getTerminal() {
        return terminal;
    }

    public void setTerminal(Terminal terminal) {
        this.terminal = terminal;
    }

    public List<Authority> getAuthorities() {
        return authorities;
    }

    public void setAuthorities(List<Authority> authorities) {
        this.authorities = authorities;
    }
}
