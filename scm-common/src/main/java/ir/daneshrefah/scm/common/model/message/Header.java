package ir.daneshrefah.scm.common.model.message;


import ir.daneshrefah.scm.common.model.terminal.TerminalServiceChannelAccess;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;

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
    private IAuthenticationHeader authentication;
    @Setter
    private Boolean secondLevelAuthenticated;
    private String correlationId;
    private String clientCorrelationId;
    private Instant clientTimestamp;
    private String clientAgent;
    private Instant receiveTimestamp;
    private String accessParameter;
    private String serverHost;
    private TerminalServiceChannelAccess service;
    private String clientAddress;

    public String getUsername() {
        if (null != authentication) { //TODO username
            return authentication.getName();
        }
        return null;
    }

    public String getTerminalCode() {
        if (null == service || null == service.getTerminalServiceAccess() ||
                null == service.getTerminalServiceAccess().getTerminal()) {
            return null;
        }
        return service.getTerminalServiceAccess().getTerminal().getCode();
    }
}
