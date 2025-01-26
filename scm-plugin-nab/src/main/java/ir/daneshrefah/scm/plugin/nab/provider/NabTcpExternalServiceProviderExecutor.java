package ir.daneshrefah.scm.plugin.nab.provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.message.MessageOutput;
import ir.daneshrefah.scm.common.model.message.TcpMessageOutput;
import ir.daneshrefah.scm.common.service.ResourceService;
import ir.daneshrefah.scm.common.service.ServiceService;
import ir.daneshrefah.scm.plugin.api.model.service.external.povider.executor.AbstractMultipleStepsExternalServiceProviderExecutor;
import ir.daneshrefah.scm.plugin.api.model.service.external.povider.executor.helper.CamelInvocationStepBuilder;
import ir.daneshrefah.scm.plugin.api.model.service.external.povider.executor.helper.Options;
import ir.daneshrefah.scm.plugin.api.model.service.external.povider.executor.helper.TcpProtocol;
import org.apache.camel.Exchange;
import org.apache.camel.model.TryDefinition;

public abstract class NabTcpExternalServiceProviderExecutor extends AbstractMultipleStepsExternalServiceProviderExecutor {

    private static final String TCP_PREFIX = "netty:tcp://";

    public NabTcpExternalServiceProviderExecutor(ObjectMapper objectMapper, ResourceService resourceService, ServiceService serviceService) {
        super(objectMapper, resourceService, serviceService);
    }


    /* TCP CONNECTION STANDARD PARTS */
    public abstract TcpProtocol getTcpProtocol();

    public abstract Options getTcpOptions();

    public abstract void connectionAcknowledge(Object body);

    public abstract Object extractServiceParametersRequestBody(Message message, Object body, MessageOutput messageOutput);
    public abstract Object extractServiceParametersResponseBody(Message message, Object body);

    protected MessageOutput buildMessageOutput() {
        return TcpMessageOutput.builder().build();
    }


    @Override
    protected CamelInvocationStepBuilder call(TryDefinition routeDefinition) {
        return CamelInvocationStepBuilder.create()
                .add(this::beforeConnect, this::internalConnect, this::afterConnect)
                .add(this::beforeCallService, this::callService, this::afterCallService);
    }


    /* START CONNECT PHASE */
    private void beforeConnect(Exchange exchange) {
        MessageOutput messageOutput = buildMessageOutput();
        Object body = getTcpProtocol().name();
        exchange.getIn().setBody(getTcpProtocol().name());
        setupMessageOutput(exchange,messageOutput,body);
    }

    private void internalConnect(TryDefinition tryDefinition) {
        tryDefinition.to(getTargetUrl());
    }

    private void afterConnect(Exchange exchange) {
        connectionAcknowledge(exchange.getMessage().getBody());
    }

    /* END CONNECT PHASE */


    /* START CALL PHASE */
    private void beforeCallService(Exchange exchange) {
        MessageOutput messageOutput = buildMessageOutput();
        Object body = extractBody(exchange, messageOutput,this::extractServiceParametersRequestBody);
        setupMessageOutput(exchange,messageOutput,body);
    }

    protected void callService(TryDefinition tryDefinition) {
        tryDefinition.to(getTargetUrl());
    }

    private void afterCallService(Exchange exchange) {
        Message originalMessage = exchange.getProperty(HEADER_ORIGINAL_MESSAGE, Message.class);
        exchange.getMessage().setBody(extractServiceParametersResponseBody(originalMessage, exchange.getMessage().getBody()));
    }

    /* START END PHASE */


    public String getTargetUrl() {
//        return TCP_PREFIX + getProviderEndpoint().orElseThrow(() -> new IllegalArgumentException("No target url provided")) +
        return TCP_PREFIX + getProviderEndpoint().orElse(null) +
               getTcpOptions().build();
    }


}
