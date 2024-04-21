package ir.daneshrefah.scm.common.model.message;


import ir.daneshrefah.scm.common.model.customer.UserProfile;
import ir.daneshrefah.scm.common.model.terminal.Channel;
import ir.daneshrefah.scm.common.model.terminal.TerminalServiceAccess;
import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

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

    private final MessageRequestInfo request;
    private Authentication authentication;
    private boolean isTransactionAuthenticated;
    private final String correlationId;
    private final Channel channel;
    private final TerminalServiceAccess serviceAccess;
    @Builder.Default
    private final int level = 1;
    private final String parentMessageId;
    private final String messageId = UUID.randomUUID().toString();

    public void authenticate(Authentication authentication) {
        this.authentication = authentication;
    }

    public void authenticateTransaction(boolean isTransactionAuthenticated) {
        this.isTransactionAuthenticated = isTransactionAuthenticated;
    }

    public UserProfile getUserProfile() {
        if (null != authentication) {
            return authentication.getProfile();
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
