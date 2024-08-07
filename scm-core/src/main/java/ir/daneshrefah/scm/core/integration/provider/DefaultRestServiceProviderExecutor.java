package ir.daneshrefah.scm.core.integration.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.exception.InvalidInputException;
import ir.daneshrefah.scm.common.exception.NoMatchRecordFoundException;
import ir.daneshrefah.scm.common.exception.RestExternalServiceProviderException;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.message.MessageOutput;
import ir.daneshrefah.scm.common.model.service.HttpContentType;
import ir.daneshrefah.scm.common.model.service.parameter.Parameter;
import ir.daneshrefah.scm.common.model.service.parameter.ParameterDatasourceCondition;
import ir.daneshrefah.scm.common.model.service.parameter.ResponseCondition;
import ir.daneshrefah.scm.common.model.transformer.Transformer;
import ir.daneshrefah.scm.common.service.ResourceService;
import ir.daneshrefah.scm.common.service.ServiceService;
import ir.daneshrefah.scm.core.mapper.ResponseConditionMapper;
import ir.daneshrefah.scm.core.service.ParameterParser;
import ir.daneshrefah.scm.plugin.api.model.service.external.AbstractRestExternalServiceProviderExecutor;
import ir.daneshrefah.scm.plugin.api.model.service.external.rest.RestExternalService;
import ir.daneshrefah.scm.utils.string.StringUtils;
import lombok.SneakyThrows;
import org.springdoc.core.service.RequestBodyService;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-07-14
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public final class DefaultRestServiceProviderExecutor extends AbstractRestExternalServiceProviderExecutor {

    private final ParameterParser parameterParser;
    private final RequestBodyService requestBodyBuilder;


    public DefaultRestServiceProviderExecutor(ResourceService resourceService, ServiceService serviceService, ObjectMapper objectMapper, ParameterParser parameterParser, RequestBodyService requestBodyBuilder) {
        super(resourceService, serviceService, objectMapper);
        this.parameterParser = parameterParser;
        this.requestBodyBuilder = requestBodyBuilder;
    }


    @Override
    protected String extractHttpMethod(Message message) {
        RestExternalService service = extractRestService(message).get();
        if (Objects.nonNull(service.getHttpMethod())) {
            return service.getHttpMethod().getValue();
        }
        if (Objects.nonNull(service.getServiceProvider().getMetadata()) &&
            Objects.nonNull(service.getServiceProvider().getMetadata().getDefaultHttpMethod())) {
            return service.getServiceProvider().getMetadata().getDefaultHttpMethod().getValue();
        }
        return super.extractContentType(message);
    }

    @Override
    protected String extractContentType(Message message) {
        RestExternalService service = extractRestService(message).get();
        HttpContentType contentType = extractContentType(service);
        return null != contentType ? contentType.getValue() : super.extractContentType(message);
    }

    private HttpContentType extractContentType(RestExternalService service) {
        if (Objects.nonNull(service.getRequestContentType())) {
            return service.getRequestContentType();
        }
        if (Objects.nonNull(service.getServiceProvider().getMetadata()) &&
            Objects.nonNull(service.getServiceProvider().getMetadata().getDefaultRequestContentType())) {
            return service.getServiceProvider().getMetadata().getDefaultRequestContentType();
        }
        return null;
    }

    @Override
    protected Map<String, ?> extractAdditionalHeaders(Message message) {
        RestExternalService service = extractRestService(message).orElseThrow(() -> new NoMatchRecordFoundException("service"));
        ParameterParser.RestExternalServiceParameterCache parameterTreeMap = parameterParser.getParametersCache(service);
        ParameterParser.ParameterNode node = parameterTreeMap.getRequestHeaderVariableNode();
        Map<String, Object> headerMap = new HashMap<>();
        parameterParser.writeHeadersVariable(node, headerMap, message, this::extractParameterValue);
        return headerMap;
    }

    @Override
    protected Map<String, ?> extractResponseHeaders(Message message) {
        RestExternalService service = extractRestService(message).orElseThrow(() -> new NoMatchRecordFoundException("service"));
        ParameterParser.RestExternalServiceParameterCache parameterTreeMap = parameterParser.getParametersCache(service);
        ParameterParser.ParameterNode node = parameterTreeMap.getResponseHeaderVariableNode();
        Map<String, Object> headerMap = new HashMap<>();
        parameterParser.writeHeadersVariable(node, headerMap, message, this::extractParameterValue);
        return headerMap;
    }

    @Override
    protected String extractTargetUrl(Message message) {
        RestExternalService service = extractRestService(message).orElseThrow(() -> new NoMatchRecordFoundException("service"));
        String providerEndpoint = extractProviderEndpoint();
        if (providerEndpoint.endsWith("/")) {
            providerEndpoint = providerEndpoint.substring(0, providerEndpoint.length() - 1);
        }
        String targetUrl = StringUtils.joinWith("/", providerEndpoint, service.getPath());
        ParameterParser.RestExternalServiceParameterCache parametersCache = parameterParser.getParametersCache(service);
        ParameterParser.ParameterNode pathVariableNode = parametersCache.getRequestPathVariableNode();
        ParameterParser.ParameterNode requestQueryNode = parametersCache.getRequestQueryStringVariableNode();
        targetUrl = parameterParser.writeRequestPathVariable(targetUrl, pathVariableNode, message, this::extractParameterValue);
        targetUrl = parameterParser.writeRequestQueryStringVariable(targetUrl, requestQueryNode, message, this::extractParameterValue);
        return targetUrl;
    }

    @Override
    protected Object extractServiceParametersRequestBody(Message message, Object body, MessageOutput messageOutput) {
        RestExternalService restService = (RestExternalService) message.getHeader().getService();
        ParameterParser.RestExternalServiceParameterCache parametersCache = parameterParser.getParametersCache(restService);
        ParameterParser.ParameterNode requestNode = parametersCache.getRequestBodyNode();
        return parameterParser.writeBodyValue(requestNode, message, this::extractParameterValue);
    }

    @Override
    protected Object extractServiceParametersResponseBody(Message message, Object body) {
        RestExternalService restService = (RestExternalService) message.getHeader().getService();
        HttpContentType contentType = extractContentType(restService);
        if (HttpContentType.RAW_JSON.equals(contentType)) {
            ParameterParser.RestExternalServiceParameterCache parametersCache = parameterParser.getParametersCache(restService);
            ParameterParser.ConditionCache conditionCache = parametersCache.getConditionCache();
            Message wrapMessage = Message.builder().payload(convertResponseToJsonNode(body)).build();
            ResponseCondition responseCondition = findResponseCondition(conditionCache, message);
            checkResponseHasException(restService,responseCondition);
            Transformer responseTransformer = responseCondition.getResponseTransformer();  //TODO
            ParameterParser.ParameterNode requestNode = conditionCache.getResponseBodyNode(responseCondition);
            return parameterParser.writeBodyValue(requestNode, wrapMessage, this::extractParameterValue);
        }
        return null;
    }

    private void checkResponseHasException(RestExternalService restService,ResponseCondition responseCondition) {
        if (Objects.isNull(responseCondition)
        || StringUtils.isNotBlank(responseCondition.getResponseExceptionErrorMessageProperty())
        || StringUtils.isNotBlank(responseCondition.getResponseExceptionErrorCodeProperty())) {
            throw new RestExternalServiceProviderException(responseCondition,restService.getServiceProvider().getCode(),restService.getCode());
        }
    }

    private ResponseCondition findResponseCondition(ParameterParser.ConditionCache conditionCache, Message message) {
        ResponseCondition providerCondition = findCompatibaleResponseCondition(conditionCache.getProviderConditionCache(),message);
        if (Objects.nonNull(providerCondition)) {
            return providerCondition;
        }
        return findCompatibaleResponseCondition(conditionCache,message);
    }

    private ResponseCondition findCompatibaleResponseCondition(ParameterParser.ConditionCache conditionCache,Message message) {
        return conditionCache
                .getConditions()
                .stream()
                .filter(responseCondition -> {
                    for (ParameterDatasourceCondition condition : responseCondition.getConditions()) {
                        Parameter wrapper = new Parameter();
                        wrapper.setDatasource(condition.getParameter());
                        if(extractParameterValue(message, wrapper).filter(extractedValue -> String.valueOf(extractedValue).equals(String.valueOf(condition.getConditionValue()))).isEmpty()){
                            return false;
                        }
                    }
                    return true;
                })
                .parallel()
                .findFirst()
                .orElse(null);
    }

    @SneakyThrows
    private JsonNode convertResponseToJsonNode(Object body) {
        InputStream inputStream = (InputStream) body;
        inputStream.reset();
        byte[] bytes = inputStream.readAllBytes();
        return objectMapper.readTree(new String(bytes, StandardCharsets.UTF_8));
    }

    private Optional<RestExternalService> extractRestService(Message message) {
        if (Objects.isNull(message) || Objects.isNull(message.getHeader()) || Objects.isNull(message.getHeader().getService()) ||
            !(message.getHeader().getService() instanceof RestExternalService)) {
            return Optional.empty();
        }
        return Optional.of((RestExternalService) message.getHeader().getService());
    }

}