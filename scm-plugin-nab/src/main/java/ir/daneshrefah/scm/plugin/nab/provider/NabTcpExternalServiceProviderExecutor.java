package ir.daneshrefah.scm.plugin.nab.provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.message.MessageOutput;
import ir.daneshrefah.scm.common.model.message.TcpMessageOutput;
import ir.daneshrefah.scm.common.service.ResourceService;
import ir.daneshrefah.scm.common.service.ServiceService;
import ir.daneshrefah.scm.plugin.api.model.service.external.povider.executor.AbstractPreparedExternalServiceProviderExecutor;
import ir.daneshrefah.scm.plugin.api.model.service.external.povider.executor.helper.CamelInvocationStep;
import ir.daneshrefah.scm.plugin.api.model.service.external.povider.executor.helper.Options;
import ir.daneshrefah.scm.plugin.api.model.service.external.povider.executor.helper.TcpProtocol;
import org.apache.camel.Exchange;
import org.apache.camel.model.TryDefinition;

import java.util.ArrayList;
import java.util.List;

import static org.apache.camel.builder.Builder.constant;

public abstract class NabTcpExternalServiceProviderExecutor extends AbstractPreparedExternalServiceProviderExecutor {

    private static final String TCP_PREFIX = "netty:tcp://";

    public NabTcpExternalServiceProviderExecutor(ObjectMapper objectMapper, ResourceService resourceService, ServiceService serviceService) {
        super(objectMapper, resourceService, serviceService);
    }

    /* TCP CONNECTION STANDARD PARTS */
    public abstract TcpProtocol getTcpProtocol();

    public abstract Options getTcpOptions();

    public abstract void connectionAcknowledge(Object body);


    @Override
    protected void beforeRouteCalling(Exchange exchange) {
        Message originalMessage = exchange.getProperty(HEADER_ORIGINAL_MESSAGE, Message.class);
        MessageOutput messageOutput = buildMessageOutput();
        messageOutput.setExternalCorrelationId(getProviderCorrelationId(originalMessage));
        Object body = getTcpProtocol().name();
        messageOutput.setBody(body);
        exchange.getMessage().setBody(messageOutput.getBody());
        exchange.setProperty(HEADER_MESSAGE_OUTPUT, messageOutput);
    }

    protected void beforeRouteCallingService(Exchange exchange) {
        super.beforeRouteCalling(exchange);
    }


    public String getTargetUrl() {
        return TCP_PREFIX + getProviderEndpoint().orElseThrow(() -> new IllegalArgumentException("No target url provided")) +
               getTcpOptions().build();
    }


    private void internalConnect(TryDefinition tryDefinition) {
        tryDefinition
                .setBody(constant(getTcpProtocol().name()))
                .to(getTargetUrl())
                .process(exchange -> connectionAcknowledge(exchange.getMessage().getBody()));
    }

    @Override
    protected void call(TryDefinition tryDefinition) {
        tryDefinition
                .process(this::beforeRouteCallingService)
                .to(getTargetUrl())
                .process(this::afterRouteCalling);
    }

    @Override
    protected MessageOutput buildMessageOutput() {
        return TcpMessageOutput.builder().build();
    }

    @Override
    protected List<CamelInvocationStep> callRoute(TryDefinition routeDefinition) {
        List<CamelInvocationStep> steps = new ArrayList<>();
        steps.add(this::internalConnect);
        steps.add(this::call);
        return steps;
    }
}
