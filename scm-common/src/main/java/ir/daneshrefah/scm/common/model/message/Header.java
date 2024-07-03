package ir.daneshrefah.scm.common.model.message;


import ir.daneshrefah.scm.common.model.condition.Condition;
import ir.daneshrefah.scm.common.model.condition.ConditionKey;
import ir.daneshrefah.scm.common.model.terminal.Channel;
import ir.daneshrefah.scm.common.model.terminal.TerminalServiceAccess;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.Instant;
import java.util.Map;
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

    private final MessageInput input;
    private Authentication authentication;
    private Boolean isTransactionAuthenticated;
    private final String correlationId;
    private final Channel channel;
    private final TerminalServiceAccess serviceAccess;
    @Setter
    private Map<ConditionKey, Condition> withdrawConditions;
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

//    public UserProfile getUserProfile() {
//        if (null != authentication) {
//            return authentication.getProfile();
//        }
//        return null;
//    }

    public String getTerminalCode() {
        return input.getTerminalCode();
    }

    public String getContentType() {
        return input.getContentType();
    }

    public Instant getReceiveTimestamp() {
        return input.getReceiveTimestamp();
    }

    public String getClientCorrelationId() {
        return input.getClientCorrelationId();
    }

    public Instant getClientTimestamp() {
        return input.getClientTimestamp();
    }

}
