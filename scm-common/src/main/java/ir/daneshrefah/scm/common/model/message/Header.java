package ir.daneshrefah.scm.common.model.message;


import ir.daneshrefah.scm.common.model.person.PersonProfile;
import ir.daneshrefah.scm.common.model.terminal.Channel;
import ir.daneshrefah.scm.common.model.terminal.TerminalServiceAccess;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.Instant;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-23
 */
@Getter
@Builder
public class Header implements Serializable {

    private String contentType;
    @Setter
    private Authentication authentication;
    @Setter
    private boolean isTransactionAuthenticated = false;
    @Setter
    private String correlationId;
    private String clientCorrelationId;
    private Instant clientTimestamp;
    private String clientAgent;
    private Instant receiveTimestamp;
    private String accessParameter;
    private String serverHost;
    private Channel channel;
    private TerminalServiceAccess serviceAccess;
    private String clientAddress;

    public String getUsername() {
        if (null != authentication) { //TODO username
            return authentication.getName();
        }
        return null;
    }

    public PersonProfile getPersonProfile() {
        if (null != authentication) {
            return authentication.getPersonProfile();
        }
        return null;
    }

    public String getTerminalCode() {
        if (null == serviceAccess || null == serviceAccess.getTerminal()) {
            return null;
        }
        return serviceAccess.getTerminal().getCode();
    }
}
