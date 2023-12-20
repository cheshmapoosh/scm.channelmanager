package ir.daneshrefah.scm.uaa.common.model.authentication;

import ir.daneshrefah.scm.common.model.message.AuthenticationHeader;
import ir.daneshrefah.scm.common.model.terminal.Terminal;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-08-14
 */
public class Authentication implements AuthenticationHeader {

    private String sessionKey;
    private String username;
    private String accessParameter;
    private Terminal terminal;
//    private List<Authority> authorities;

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

}
