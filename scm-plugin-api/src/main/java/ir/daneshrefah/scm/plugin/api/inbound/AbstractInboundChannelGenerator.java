package ir.daneshrefah.scm.plugin.api.inbound;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.model.error.Error;
import ir.daneshrefah.scm.common.model.error.ErrorReason;
import ir.daneshrefah.scm.common.model.error.ErrorType;
import ir.daneshrefah.scm.common.model.message.Authentication;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.message.Status;
import ir.daneshrefah.scm.common.model.terminal.Channel;
import ir.daneshrefah.scm.common.model.terminal.TerminalServiceChannelAccess;
import ir.daneshrefah.scm.common.model.transformer.TransformerRelation;
import ir.daneshrefah.scm.common.model.transformer.TransformerRelationType;
import ir.daneshrefah.scm.logging.api.EventProducer;
import ir.daneshrefah.scm.logging.domain.event.AuthenticationEvent;
import ir.daneshrefah.scm.logging.domain.event.Event;
import ir.daneshrefah.scm.logging.domain.event.InboundEvent;
import ir.daneshrefah.scm.logging.domain.event.ResponseBuildEvent;
import ir.daneshrefah.scm.plugin.api.authority.decision.DecisionManager;
import ir.daneshrefah.scm.plugin.api.authority.decision.PermitAllDecisionManager;
import ir.daneshrefah.scm.plugin.api.integration.ServiceProducerTemplate;
import ir.daneshrefah.scm.plugin.api.service.TransformerService;
import ir.daneshrefah.scm.plugin.api.transformer.TransformerExecutionWrapper;
import ir.daneshrefah.scm.uaa.client.ClientAuthenticationException;
import ir.daneshrefah.scm.uaa.client.core.AuthenticationClientTemplate;
import ir.daneshrefah.scm.uaa.client.core.ClientAuthenticationRequest;
import ir.daneshrefah.scm.uaa.common.model.authentication.UserAuthentication;
import ir.daneshrefah.scm.utils.ClassUtils;
import ir.daneshrefah.scm.utils.constant.Constants;
import ir.daneshrefah.scm.utils.string.StringUtils;
import lombok.AccessLevel;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-22
 */
public abstract class AbstractInboundChannelGenerator<T> {

    protected static final Logger LOGGER = LoggerFactory.getLogger(AbstractInboundChannelGenerator.class);

    @Getter(AccessLevel.PROTECTED)
    private final ObjectMapper objectMapper;
    @Getter(AccessLevel.PROTECTED)
    private Channel channel;
    @Getter(AccessLevel.PROTECTED)
    private JsonNode metadata;
    @Getter(AccessLevel.PROTECTED)
    private List<TerminalServiceChannelAccess> services;
    private final AuthenticationClientTemplate authenticationTemplate;
    private final MessageBuilder<T> messageBuilder;
    private final ResponseBuilder<T> responseBuilder;
    private final DecisionManager decisionManager;
    private final ServiceProducerTemplate producerTemplate;
    private final TransformerService transformerService;
    private Map<String, List<TransformerExecutionWrapper>> requestTransformerMap = new HashMap<>();

    protected AbstractInboundChannelGenerator(ObjectMapper objectMapper, AuthenticationClientTemplate authenticationTemplate,
                                              ServiceProducerTemplate producerTemplate,
                                              TransformerService transformerService,
                                              MessageBuilder<T> messageBuilder, ResponseBuilder<T> responseBuilder,
                                              DecisionManager decisionManager) {
        this.objectMapper = objectMapper;
        this.authenticationTemplate = authenticationTemplate;
        this.producerTemplate = producerTemplate;
        this.transformerService = transformerService;
        this.messageBuilder = messageBuilder;
        this.responseBuilder = responseBuilder;
        this.decisionManager = null != decisionManager ? decisionManager : new PermitAllDecisionManager();
    }

    public final boolean initConfig(Channel channel, JsonNode metadata) {
        this.channel = channel;
        this.metadata = metadata;

        return initConfig();
    }

    public abstract boolean initConfig();

    public final boolean registerEndpoints(List<TerminalServiceChannelAccess> services) {
        this.services = services;

        for (Iterator<TerminalServiceChannelAccess> iterator = services.iterator(); iterator.hasNext(); ) {
            TerminalServiceChannelAccess channelAccess = iterator.next();
            String terminalId = channelAccess.getTerminalServiceAccess().getTerminal().getId();
            List<TransformerRelation> terminalServiceRequestTransformers = transformerService
                    .findAllTransformerRelationsBySourceAndType(channelAccess.getTerminalServiceAccess().getId(), TransformerRelationType.TERMINAL_SERVICE_REQUEST);
            List<TransformerExecutionWrapper> requestTransformers = terminalServiceRequestTransformers.stream().map(t -> new TransformerExecutionWrapper(t)).collect(Collectors.toList());
            requestTransformerMap.put(terminalId + "_" + channelAccess.getTerminalServiceAccess().getService().getId(), requestTransformers);
            if (!requestTransformerMap.containsKey(terminalId)) {
                terminalServiceRequestTransformers = transformerService
                        .findAllTransformerRelationsBySourceAndType(channelAccess.getTerminalServiceAccess().getTerminal().getId(),
                                TransformerRelationType.TERMINAL_REQUEST);
                requestTransformers = terminalServiceRequestTransformers.stream().map(t -> new TransformerExecutionWrapper(t)).collect(Collectors.toList());
                requestTransformerMap.put(terminalId, requestTransformers);
            }
        }

        return registerEndpoints();
    }

    protected abstract boolean registerEndpoints();

    protected final TerminalServiceChannelAccess findService(String terminalCode, String serviceCode) {
        if (StringUtils.isEmpty(terminalCode) || StringUtils.isEmpty(serviceCode))
            return null;

        if (null == serviceCode || services.size() < 1)
            return null;

        return services.stream()
                .filter(service ->
                        service.getTerminalServiceAccess().getTerminal().getCode().equals(terminalCode) &&
                                service.getTerminalServiceAccess().getService().getCode().equals(serviceCode))
                .findFirst()
                .orElse(null);
    }

    protected final Message buildMessage(T input, TerminalServiceChannelAccess service) {
        Message message = messageBuilder.build(input, service);
        ClientAuthenticationRequest authenticationRequest = extractAuthenticationRequest(input);
        UserAuthentication authentication = authenticateUser(message, authenticationRequest, false);
        UserAuthentication transactionAuthentication = authenticateUser(message, authenticationRequest, true);
        message.getHeader().setAuthentication(authentication);
        message.getHeader().setTransactionAuthenticated(null != transactionAuthentication &&
                transactionAuthentication.isAuthenticated() && !transactionAuthentication.isAnonymous());
        if (authentication.hasError() || transactionAuthentication.hasError()) {
            message.addError(new Error(ErrorType.AUTHENTICATION_FAILED, Constants.SCM_PARAMETER_AUTHORIZATION,
                    ErrorReason.IS_INVALID), Status.SC_UNAUTHORIZED);
            message.setPayload(objectMapper.nullNode());
        }
        logIncomingMessage(message);
        return message;
    }

    protected final T buildResponse(T input, Message message) {
        Instant startTime = Instant.now();
        T response = responseBuilder.build(input, message);
        logResponseGenerationEvent(message, message.getPayload(), startTime);
        return response;
    }

    protected abstract ClientAuthenticationRequest extractAuthenticationRequest(T input);

    private boolean checkServiceCallAllowed(Message message) {
        return decisionManager.decide(message);
    }

    private final void logIncomingMessage(Message message) {
        Event event = InboundEvent.builder()
                .correlationId(message.getHeader().getCorrelationId())
                .clientCorrelationId(message.getHeader().getClientCorrelationId())
                .startTimestamp(Instant.now())
                .terminalCode(message.getHeader().getService().getTerminalServiceAccess().getTerminal().getCode())
                .threadName(Thread.currentThread().getName())
                .sourceClassName(this.getClass().getSimpleName())
//                .input(message)
                .serverHost(message.getHeader().getServerHost())
                .clientAgent(message.getHeader().getClientAgent())
                .build();
        EventProducer.getInstance().sendEvent(event);
    }

    private final void logResponseGenerationEvent(Message message, JsonNode response, Instant startTime) {
        Instant endTime = Instant.now();
        Event event = ResponseBuildEvent.builder()
                .correlationId(message.getHeader().getCorrelationId())
                .clientCorrelationId(message.getHeader().getClientCorrelationId())
                .startTimestamp(startTime)
                .terminalCode(message.getHeader().getTerminalCode())
                .threadName(Thread.currentThread().getName())
                .sourceClassName(this.getClass().getSimpleName())
//                .input(response)
                .clientAgent(message.getHeader().getClientAgent())
                .endTimestamp(endTime)
                .durationMillis(Duration.between(startTime, endTime).toMillis())
                .build();
        EventProducer.getInstance().sendEvent(event);
    }

    protected UserAuthentication authenticateUser(Message message, ClientAuthenticationRequest authenticationRequest,
                                                  boolean isTransaction) {
        Instant startTime = Instant.now();

        UserAuthentication authentication = null;
        Exception error = null;
        try {
            if (isTransaction) {
                authentication = authenticationTemplate.authenticateTransactionByAuthenticationRequest(
                        authenticationRequest);
            } else {
                authentication = authenticationTemplate.authenticateUserByAuthenticationRequest(
                        authenticationRequest);
            }
        } catch (ClientAuthenticationException e) {
            authentication = e.getAuthentication();
            error = (Exception) ClassUtils.cloneExceptionWithoutStackTrace(null != e.getCause() ? e.getCause() : e);
        } catch (Exception e) {
            LOGGER.error("error on authentication", e);
            error = ClassUtils.cloneExceptionWithoutStackTrace(e);
        }
        logAuthenticationEvent(message, authenticationRequest, authentication, startTime, error);

        return authentication;
    }

    private void logAuthenticationEvent(Message message, ClientAuthenticationRequest authenticationRequest,
                                        Authentication authentication, Instant startTime, Exception error) {
        Instant endTime = Instant.now();
        Event event = AuthenticationEvent.builder()
                .correlationId(message.getHeader().getCorrelationId())
                .clientCorrelationId(message.getHeader().getClientCorrelationId())
                .startTimestamp(startTime)
                .terminalCode(message.getHeader().getTerminalCode())
                .threadName(Thread.currentThread().getName())
                .sourceClassName(this.getClass().getSimpleName())
                .input(authenticationRequest)
                .output(authentication)
                .error(error)
                .clientAgent(message.getHeader().getClientAgent())
                .serverHost(message.getHeader().getServerHost())
                .endTimestamp(endTime)
                .durationMillis(Duration.between(startTime, endTime).toMillis())
                .build();
        EventProducer.getInstance().sendEvent(event);
    }

    protected final Message executeService(Message message) {
        if (!checkServiceCallAllowed(message)) {
            message.addAccessDeniedError(Constants.SCM_PARAMETER_AUTHORIZATION);
            return message;
        }

        TerminalServiceChannelAccess service = message.getHeader().getService();
        List<TransformerExecutionWrapper> transformerRelations = extractRequestTransformerList(service);
        JsonNode payload = message.getPayload();
        for (Iterator<TransformerExecutionWrapper> iterator = transformerRelations.iterator(); iterator.hasNext(); ) {
            TransformerExecutionWrapper transformerExecutionWrapper = iterator.next();
            payload = (JsonNode) transformerExecutionWrapper.getTransformerInstance()
                    .transform(payload, message, transformerExecutionWrapper.getTransformerRelation().getMetadata());

        }
        message.setPayload(payload);
        producerTemplate.callService(service.getTerminalServiceAccess().getService(), message);

        return message;
    }

    private List<TransformerExecutionWrapper> extractRequestTransformerList(TerminalServiceChannelAccess service) {
        String terminalId = service.getTerminalServiceAccess().getTerminal().getId();
        String serviceId = service.getTerminalServiceAccess().getService().getId();
        List<TransformerExecutionWrapper> result = new ArrayList<>(requestTransformerMap.get(terminalId));
        result.addAll(requestTransformerMap.get(terminalId + "_" + serviceId));
        return result;
    }

}
