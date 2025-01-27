package ir.daneshrefah.scm.plugin.api.model.service.external.povider.executor;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.service.AbstractExternalServiceProvider;
import ir.daneshrefah.scm.common.model.service.ProviderTerminalCoding;
import ir.daneshrefah.scm.common.service.ResourceService;
import ir.daneshrefah.scm.common.service.ServiceService;
import ir.daneshrefah.scm.plugin.api.model.service.external.AbstractExternalService;
import ir.daneshrefah.scm.plugin.api.model.service.external.ExternalServiceProviderExecutor;
import ir.daneshrefah.scm.plugin.api.model.service.external.povider.executor.helper.CamelInvocationStep;
import ir.daneshrefah.scm.uaa.common.utils.AuthenticationUtils;
import ir.daneshrefah.scm.utils.MessageInputContext;
import ir.daneshrefah.scm.utils.string.StringUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.camel.Exchange;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.model.TryDefinition;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@RequiredArgsConstructor
public abstract class AbstractBaseExternalServiceProviderExecutor implements ExternalServiceProviderExecutor {

    protected final ObjectMapper objectMapper;
    private final ResourceService resourceService;
    private final ServiceService serviceService;

    @Getter
    private AbstractExternalServiceProvider serviceProvider;

    public final void init(AbstractExternalServiceProvider provider) {
        this.serviceProvider = provider;
    }

    @Override
    public void endpointCallRouteDefinition(RouteDefinition routeDefinition) {
        TryDefinition tryDefinition = routeDefinition.doTry();
        tryDefinition.process(this::startLog);
        callRoute(tryDefinition).forEach(camelInvocationStep -> {
            tryDefinition.process(exchange ->adviseBeforeStepCall(camelInvocationStep,exchange));
            camelInvocationStep.callStepRoute(tryDefinition);
            tryDefinition.process(exchange -> adviseAfterStepCall(camelInvocationStep,exchange));
        });
        tryDefinition.doFinally();
        tryDefinition.process(this::endLog);
        tryDefinition.endDoTry();
    }

    private void endLog(Exchange exchange) {
        //TODO ALIREZA
    }

    private void startLog(Exchange exchange) {
        //TODO ALIREZA
    }

    private void startStepLog(Exchange exchange) {
        //TODO ALIREZA
    }

    private void endStepLog(Exchange exchange) {
        //TODO ALIREZA
    }

    protected abstract List<CamelInvocationStep> callRoute(TryDefinition routeDefinition);

    private void adviseBeforeStepCall(CamelInvocationStep camelInvocationStep,Exchange exchange){
        startStepLog(exchange);
        camelInvocationStep.beforeStepRouteCalling(exchange);
    }

    private void adviseAfterStepCall(CamelInvocationStep camelInvocationStep,Exchange exchange){
        camelInvocationStep.afterStepRouteCalling(exchange);
        endStepLog(exchange);
    }


    private Exception extractException(Exchange exchange) {
        Exception exception = exchange.getException();
        if (Objects.isNull(exception)) {
            exception = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
        }
        if (Objects.isNull(exception)) {
            exception = exchange.getProperty(Exchange.EXCEPTION_HANDLED, Exception.class);
        }
        return exception;
    }

    public String getProviderCorrelationId(Message message) {
        return MessageInputContext.getCurrentContext().getCorrelationId();
    }

    protected Optional<String> getProviderEndpoint() {
        if (Objects.nonNull(serviceProvider)
            && Objects.nonNull(serviceProvider.getMetadata())
            && StringUtils.isNotEmpty(serviceProvider.getMetadata().getEndpoint())) {
            String result = resourceService.prepareProperties(serviceProvider.getMetadata().getEndpoint());
            return Optional.ofNullable(result);
        }
        return Optional.empty();
    }

    protected final Optional<String> prepareTerminalCode(AbstractExternalService<?> service, String defaultValue) {
        String terminalCode = AuthenticationUtils.getLoggedInTerminalCode().orElse(null);
        String clientId = AuthenticationUtils.getLoggedInClientId().orElse(null);
        String providerCode = service.getServiceProvider().getCode();
        Optional<ProviderTerminalCoding> providerTerminalCoding = serviceService.findProviderTerminalCoding(terminalCode, clientId, providerCode);
        return providerTerminalCoding.flatMap(po -> Optional.ofNullable(po.getCode()))
                .or(() -> Optional.ofNullable(defaultValue));
    }

    public AbstractExternalService<?> getService(Exchange exchange){
        Message originalMessage = exchange.getProperty(HEADER_ORIGINAL_MESSAGE, Message.class);
        return (AbstractExternalService<?>) originalMessage.getHeader().getService();
    }

    @Override
    public AbstractExternalServiceProvider getProviderModel() {
        return this.serviceProvider;
    }


}
