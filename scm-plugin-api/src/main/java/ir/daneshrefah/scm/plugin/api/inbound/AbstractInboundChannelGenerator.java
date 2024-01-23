package ir.daneshrefah.scm.plugin.api.inbound;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.exception.AccessDeniedException;
import ir.daneshrefah.scm.common.model.error.Error;
import ir.daneshrefah.scm.common.model.error.ErrorCodes;
import ir.daneshrefah.scm.common.model.message.*;
import ir.daneshrefah.scm.common.model.person.PersonProfile;
import ir.daneshrefah.scm.common.model.terminal.Channel;
import ir.daneshrefah.scm.common.model.terminal.TerminalServiceAccess;
import ir.daneshrefah.scm.common.model.transformer.TransformerRelation;
import ir.daneshrefah.scm.common.model.transformer.TransformerRelationType;
import ir.daneshrefah.scm.logging.api.EventProducer;
import ir.daneshrefah.scm.logging.domain.event.Event;
import ir.daneshrefah.scm.logging.domain.event.EventType;
import ir.daneshrefah.scm.plugin.api.authority.decision.DecisionManager;
import ir.daneshrefah.scm.plugin.api.authority.decision.PermitAllDecisionManager;
import ir.daneshrefah.scm.plugin.api.integration.ServiceProducerTemplate;
import ir.daneshrefah.scm.plugin.api.model.service.external.ExternalService;
import ir.daneshrefah.scm.plugin.api.service.CustomerDataProviderService;
import ir.daneshrefah.scm.plugin.api.service.TransformerService;
import ir.daneshrefah.scm.plugin.api.transformer.TransformerExecutionWrapper;
import ir.daneshrefah.scm.uaa.client.ClientAuthenticationException;
import ir.daneshrefah.scm.uaa.client.core.AuthenticationClientTemplate;
import ir.daneshrefah.scm.uaa.client.core.ClientAuthenticationRequest;
import ir.daneshrefah.scm.uaa.common.model.authentication.UserAuthentication;
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

import static ir.daneshrefah.scm.common.model.error.ErrorCodes.ERROR_CODE_VALIDATION_PROVIDER_CUSTOMER_NOT_FOUND;
import static ir.daneshrefah.scm.utils.constant.Constants.SCM_PARAMETER_ACCESS_PARAMETER;
import static ir.daneshrefah.scm.utils.constant.Constants.SCM_PARAMETER_TERMINAL;

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
    private List<TerminalServiceAccess> services;
    private final AuthenticationClientTemplate authenticationTemplate;
//    private final MessageBuilder<T> messageBuilder;
    private final ResponseBuilder<T> responseBuilder;
    private final DecisionManager decisionManager;
    private final ServiceProducerTemplate producerTemplate;
    private final TransformerService transformerService;
    private final CustomerDataProviderService customerService;
    private Map<String, List<TransformerExecutionWrapper>> requestTransformerMap = new HashMap<>();

    protected AbstractInboundChannelGenerator(ObjectMapper objectMapper, AuthenticationClientTemplate authenticationTemplate,
                                              ServiceProducerTemplate producerTemplate,
                                              TransformerService transformerService,
                                              ResponseBuilder<T> responseBuilder,
                                              DecisionManager decisionManager,
                                              CustomerDataProviderService customerService) {
        this.objectMapper = objectMapper;
        this.authenticationTemplate = authenticationTemplate;
        this.producerTemplate = producerTemplate;
        this.transformerService = transformerService;
        this.responseBuilder = responseBuilder;
        this.decisionManager = null != decisionManager ? decisionManager : new PermitAllDecisionManager();
        this.customerService = customerService;
    }

    public final boolean initConfig(Channel channel, JsonNode metadata) {
        this.channel = channel;
        this.metadata = metadata;

        return initConfig();
    }

    public abstract boolean initConfig();

    public final boolean registerEndpoints(List<TerminalServiceAccess> services) {
        this.services = services;

        for (Iterator<TerminalServiceAccess> iterator = services.iterator(); iterator.hasNext(); ) {
            TerminalServiceAccess serviceAccess = iterator.next();
            String terminalId = serviceAccess.getTerminal().getId();
            List<TransformerRelation> terminalServiceRequestTransformers = transformerService
                    .findAllTransformerRelationsBySourceAndType(serviceAccess.getId(), TransformerRelationType.TERMINAL_SERVICE_REQUEST);
            List<TransformerExecutionWrapper> requestTransformers = terminalServiceRequestTransformers.stream().map(t -> new TransformerExecutionWrapper(t)).collect(Collectors.toList());
            requestTransformerMap.put(terminalId + "_" + serviceAccess.getService().getId(), requestTransformers);
            if (!requestTransformerMap.containsKey(terminalId)) {
                terminalServiceRequestTransformers = transformerService
                        .findAllTransformerRelationsBySourceAndType(serviceAccess.getTerminal().getId(),
                                TransformerRelationType.TERMINAL_REQUEST);
                requestTransformers = terminalServiceRequestTransformers.stream().map(t -> new TransformerExecutionWrapper(t)).collect(Collectors.toList());
                requestTransformerMap.put(terminalId, requestTransformers);
            }
        }

        return registerEndpoints();
    }

    protected abstract boolean registerEndpoints();

    protected final TerminalServiceAccess findService(String terminalCode, String serviceCode) {
        if (StringUtils.isEmpty(terminalCode) || StringUtils.isEmpty(serviceCode))
            return null;

        if (null == serviceCode || services.size() < 1)
            return null;

        return services.stream()
                .filter(service ->
                        service.getTerminal().getCode().equals(terminalCode) &&
                                service.getService().getCode().equals(serviceCode))
                .findFirst()
                .orElse(null);
    }

    protected final Message buildMessage(T input, TerminalServiceAccess serviceAccess) {
        MessageBuildRequest request = extractMessageBuildRequest(input, serviceAccess);
        ClientAuthenticationRequest authenticationRequest = extractAuthenticationRequest(input);
        return buildMessage(request, authenticationRequest, serviceAccess);
    }

    protected final Message buildMessage(MessageBuildRequest request, ClientAuthenticationRequest authenticationRequest,
                                         TerminalServiceAccess serviceAccess) {
        Message message = buildMessageObject(request, serviceAccess);
        logIncomingMessage(request, message, null, request.getReceiveTimestamp());

        if (!Status.SC_PROCESSING.equals(message.getStatus())) {
            return message;
        }

        UserAuthentication authentication = authenticateUser(message, authenticationRequest, false);
        UserAuthentication transactionAuthentication = authenticateUser(message, authenticationRequest, true);
        message.getHeader().setAuthentication(authentication);
        message.getHeader().setTransactionAuthenticated(null != transactionAuthentication &&
                transactionAuthentication.isAuthenticated() && !transactionAuthentication.isAnonymous());
        if (authentication.hasError() || transactionAuthentication.hasError()) {
            String errorMessage = authentication.hasError() ? authentication.getError() : transactionAuthentication.getError();
            if (StringUtils.isEmpty(errorMessage)) {
                errorMessage = "error on authenticate user.";
            }
            message.addError(new Error(Constants.SCM_PARAMETER_AUTHORIZATION,
                    ErrorCodes.ERROR_CODE_AUTHENTICATION_FAILED, errorMessage), Status.SC_UNAUTHORIZED);
            message.nullPayload();
        }
        return message;
    }

    protected abstract MessageBuildRequest extractMessageBuildRequest(T input, TerminalServiceAccess serviceAccess);

    private Message buildMessageObject(MessageBuildRequest request, TerminalServiceAccess serviceAccess) {
        Header header = Header.builder()
                .contentType(request.getContentType())
                .authentication(null)
                .isTransactionAuthenticated(false)
                .correlationId(StringUtils.generateGuid())
                .clientCorrelationId(request.getClientCorrelationId())
                .clientTimestamp(request.getClientTimestamp())
                .receiveTimestamp(request.getReceiveTimestamp())
                .accessParameter(request.getAccessParameter())
                .clientAgent(request.getClientAgent())
                .serverHost(request.getServerHost())
                .serviceAccess(serviceAccess)
                .channel(getChannel())
                .clientAddress(request.getClientAddress())
                .build();

        if (!request.isForCheck() && StringUtils.isEmpty(header.getAccessParameter())) {
            return createValidationErrorMessage(request, header, SCM_PARAMETER_ACCESS_PARAMETER,
                    ErrorCodes.ERROR_CODE_ACCESS_PARAMETER_IS_EMPTY, SCM_PARAMETER_ACCESS_PARAMETER + " is empty.");
        }
        if (!request.isForCheck() && StringUtils.isEmpty(request.getTerminalCode())) {
            return createValidationErrorMessage(request, header, SCM_PARAMETER_TERMINAL,
                    ErrorCodes.ERROR_CODE_TERMINAL_CODE_IS_EMPTY, SCM_PARAMETER_TERMINAL + " is empty.");
        }

        if (!request.isForCheck() && !StringUtils.equals(serviceAccess.getTerminal().getCode(), request.getTerminalCode())) {
            return createValidationErrorMessage(request, header, SCM_PARAMETER_TERMINAL,
                    ErrorCodes.ERROR_CODE_TERMINAL_CODE_IS_INVALID, SCM_PARAMETER_TERMINAL + " is invalid.");
        }

        Message message = new Message(request);
        message.setHeader(header);
        message.setStatus(request.isForCheck() ? Status.SC_SUCCESS : Status.SC_PROCESSING);
        message.setPayload(request.getPayload());

        return message;
    }

    private Message createValidationErrorMessage(MessageBuildRequest request, Header header, String source,
                                                 Integer errorCode, String errorMessage) {
        Message result = new Message(request);
        result.setHeader(header);
        result.addError(new Error(source, errorCode, errorMessage), Status.SC_ERROR_VALIDATION);
        result.setPayload(objectMapper.nullNode());
        return result;
    }

    protected final T buildResponse(T input, Message message) {
        Instant startTime = Instant.now();
        T response = responseBuilder.build(input, message);
        logResponseGenerationEvent(message);
        return response;
    }

    protected abstract ClientAuthenticationRequest extractAuthenticationRequest(T input);

    private boolean checkServiceCallAllowed(Message message) {
        return decisionManager.decide(message);
    }

    private final void logIncomingMessage(MessageBuildRequest request, Message message, Exception error, Instant startTime) {
        Instant endTime = Instant.now();
        Event event = Event.builder()
                .type(EventType.INBOUND)
                .status(message.getStatus())
                .correlationId(message.getHeader().getCorrelationId())
                .source(message.getHeader().getServiceAccess().getService().getCode())
                .terminalCode(request.getTerminalCode())
                .channelCode(request.getChannelCode())
                .startTime(startTime)
                .endTime(endTime)
                .durationMillis(Duration.between(startTime, endTime).toMillis())
                .threadName(Thread.currentThread().getName())
                .input(request)
                .output(message.getPayload())
                .error(error)
                .sourceClassName(this.getClass().getSimpleName())
                .build();
        EventProducer.getInstance().sendEvent(event);
    }

    private void logAuthenticationEvent(ClientAuthenticationRequest request, Message message,
                                        Authentication authentication, Exception error, Instant startTime) {
        Instant endTime = Instant.now();
        Event event = Event.builder()
                .type(EventType.AUTHENTICATION)
                .status(message.getStatus())
                .correlationId(message.getHeader().getCorrelationId())
                .source(null)
                .terminalCode(message.getHeader().getServiceAccess().getTerminal().getCode())
                .channelCode(message.getHeader().getChannel().getCode())
                .startTime(startTime)
                .endTime(endTime)
                .durationMillis(Duration.between(startTime, endTime).toMillis())
                .threadName(Thread.currentThread().getName())
                .input(request)
                .output(authentication)
                .error(error)
                .sourceClassName(this.getClass().getSimpleName())
                .build();
        EventProducer.getInstance().sendEvent(event);
    }

    private final void logResponseGenerationEvent(Message message) {
        Instant endTime = Instant.now();
        Event event = Event.builder()
                .type(EventType.OUTBOUND)
                .status(message.getStatus())
                .correlationId(message.getHeader().getCorrelationId())
                .source(message.getHeader().getServiceAccess().getService().getCode())
                .terminalCode(message.getHeader().getServiceAccess().getTerminal().getCode())
                .channelCode(message.getHeader().getChannel().getCode())
                .startTime(message.getHeader().getReceiveTimestamp())
                .endTime(endTime)
                .durationMillis(Duration.between(message.getHeader().getReceiveTimestamp(), endTime).toMillis())
                .threadName(Thread.currentThread().getName())
                .input(message.getRequest())
                .output(Status.SC_SUCCESS.equals(message.getStatus()) ? message.getPayload() : message.getErrors())
                .error(null)
                .sourceClassName(this.getClass().getSimpleName())
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
            error = null != e.getCause() ? (Exception) e.getCause() : e;
        } catch (Exception e) {
            LOGGER.error("error on authentication", e);
            error = e;
        }
        logAuthenticationEvent(authenticationRequest, message, authentication, error, startTime);

        return authentication;
    }


    protected final Message executeService(Message message) {
        try {
            if (!checkServiceCallAllowed(message)) {
                message.addAccessDeniedError(Constants.SCM_PARAMETER_AUTHORIZATION, null, null);
                return message;
            }
        } catch (AccessDeniedException e) {
            message.addAccessDeniedError(e.getSource(), e.getErrorCode(), e.getMessage());
            return message;
        }

        TerminalServiceAccess serviceAccess = message.getHeader().getServiceAccess();

        boolean isCustomerLoadedIfRequired = checkCustomerInfoIsLoaded(message);
        if (!isCustomerLoadedIfRequired) {
            message.addAccessDeniedError(null, ERROR_CODE_VALIDATION_PROVIDER_CUSTOMER_NOT_FOUND,
                    "no customer found for provider.");
            return message;
        }

        List<TransformerExecutionWrapper> transformerRelations = extractRequestTransformerList(serviceAccess);
        JsonNode payload = message.getPayload();
        for (Iterator<TransformerExecutionWrapper> iterator = transformerRelations.iterator(); iterator.hasNext(); ) {
            TransformerExecutionWrapper transformerExecutionWrapper = iterator.next();
            payload = (JsonNode) transformerExecutionWrapper.getTransformerInstance()
                    .transform(payload, message, transformerExecutionWrapper.getTransformerRelation().getMetadata());

        }
        message.setPayload(payload);
        producerTemplate.callService(serviceAccess.getService(), message);

        return message;
    }

    private boolean checkCustomerInfoIsLoaded(Message message) {
        TerminalServiceAccess serviceAccess = message.getHeader().getServiceAccess();
        PersonProfile profile = message.getHeader().getPersonProfile();
        ExternalService service = serviceAccess.getService() instanceof ExternalService ? (ExternalService) serviceAccess.getService() : null;
        if (null == service || !service.getServiceProvider().isCustomerProvided() || !service.isCustomerBased()) {
            return true;
        }
        if (null == profile) {
            return false;
        }
        if (profile.isCustomerLoaded(service.getServiceProvider().getId())) {
            return true;
        }
        customerService.fillCustomerForPersonProfile(profile, service.getServiceProvider());
        return profile.isCustomerLoaded(service.getServiceProvider().getId());
    }

    private List<TransformerExecutionWrapper> extractRequestTransformerList(TerminalServiceAccess serviceAccess) {
        String terminalId = serviceAccess.getTerminal().getId();
        String serviceId = serviceAccess.getService().getId();
        List<TransformerExecutionWrapper> result = new ArrayList<>(requestTransformerMap.get(terminalId));
        result.addAll(requestTransformerMap.get(terminalId + "_" + serviceId));
        return result;
    }

}
