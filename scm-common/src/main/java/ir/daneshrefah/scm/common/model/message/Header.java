package ir.daneshrefah.scm.common.model.message;


import ir.daneshrefah.scm.common.model.authentication.Authentication;
import ir.daneshrefah.scm.common.model.terminal.Terminal;
import ir.daneshrefah.scm.common.model.terminal.Channel;
import ir.daneshrefah.scm.common.model.terminal.TerminalServiceChannelAccess;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-23
 */
public class Header implements Serializable {

    private String contentType;
    private Authentication authentication;
    private String clientCorrelationId;
    private String correlationId;
    private LocalDateTime clientTransactionTimestamp;
    private LocalDateTime receiveTimestamp;
    private Terminal terminal;
    private Channel channel;
    private String accessParameter;
    private TerminalServiceChannelAccess service;

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Authentication getAuthentication() {
        return authentication;
    }

    public void setAuthentication(Authentication authentication) {
        this.authentication = authentication;
    }

    public String getClientCorrelationId() {
        return clientCorrelationId;
    }

    public void setClientCorrelationId(String clientCorrelationId) {
        this.clientCorrelationId = clientCorrelationId;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public LocalDateTime getClientTransactionTimestamp() {
        return clientTransactionTimestamp;
    }

    public void setClientTransactionTimestamp(LocalDateTime clientTransactionTimestamp) {
        this.clientTransactionTimestamp = clientTransactionTimestamp;
    }

    public LocalDateTime getReceiveTimestamp() {
        return receiveTimestamp;
    }

    public void setReceiveTimestamp(LocalDateTime receiveTimestamp) {
        this.receiveTimestamp = receiveTimestamp;
    }

    public Terminal getTerminal() {
        return terminal;
    }

    public void setTerminal(Terminal terminal) {
        this.terminal = terminal;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public String getAccessParameter() {
        return accessParameter;
    }

    public void setAccessParameter(String accessParameter) {
        this.accessParameter = accessParameter;
    }

    public TerminalServiceChannelAccess getService() {
        return service;
    }

    public void setService(TerminalServiceChannelAccess service) {
        this.service = service;
    }
}
