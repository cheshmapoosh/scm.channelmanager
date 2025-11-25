package ir.daneshrefah.scm.common.model.operation;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TcpConfigOperationDefinition extends OperationDefinition {
    private String url;
    private String command;
    private Integer connectTimeout;
    private Integer requestTimeout;
    private Boolean validateAck;
    private String ackEquals;
    private boolean tcpNoDelay;
    private boolean keepAlive;
}
