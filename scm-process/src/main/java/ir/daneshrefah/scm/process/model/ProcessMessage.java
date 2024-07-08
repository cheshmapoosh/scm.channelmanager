package ir.daneshrefah.scm.process.model;

import ir.daneshrefah.scm.common.model.terminal.Channel;
import lombok.Data;

import java.io.Serializable;

@Data
public class ProcessMessage implements Serializable {//TODO delete it
    private String delegateUsername;
    private String correlationId;
    private String processCorrelationId;
    private String processDefinitionKey;
    private String processInstanceId;
    private String taskId;
    private String taskName;
    private String terminalCode;
    private Channel channel;
    private String accessParameter;
}
