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

    private final MessageBuildRequest request;
    private Authentication authentication;
    private boolean isTransactionAuthenticated;
    private String correlationId;
    private Channel channel;
    private TerminalServiceAccess serviceAccess;

    public void authenticate(Authentication authentication) {
        this.authentication = authentication;
    }

    public void authenticateTransaction(boolean isTransactionAuthenticated) {
        this.isTransactionAuthenticated = isTransactionAuthenticated;
    }

    public PersonProfile getPersonProfile() {
        if (null != authentication) {
            return authentication.getPersonProfile();
        }
        return null;
    }

    public String getTerminalCode() {
        return request.getTerminalCode();
    }

    public String getContentType() {
        return request.getContentType();
    }

    public Instant getReceiveTimestamp() {
        return request.getReceiveTimestamp();
    }

    public String getClientCorrelationId() {
        return request.getClientCorrelationId();
    }

    public Instant getClientTimestamp() {
        return request.getClientTimestamp();
    }

}
