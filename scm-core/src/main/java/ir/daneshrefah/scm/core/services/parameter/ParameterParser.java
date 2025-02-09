package ir.daneshrefah.scm.core.services.parameter;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.NumericNode;
import ir.daneshrefah.scm.common.data.converter.PersonTypeConverter;
import ir.daneshrefah.scm.common.model.dynamic.rest.ParameterNode;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.service.Service;
import ir.daneshrefah.scm.common.model.service.parameter.Parameter;
import ir.daneshrefah.scm.common.model.service.parameter.ParameterActionType;
import ir.daneshrefah.scm.common.model.service.parameter.ParameterType;
import ir.daneshrefah.scm.common.model.service.parameter.Response;
import ir.daneshrefah.scm.plugin.api.model.service.external.rest.RestExternalService;
import ir.daneshrefah.scm.utils.string.StringUtils;
import jakarta.persistence.AttributeConverter;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


@Component
@RequiredArgsConstructor
public class ParameterParser {

    private static final Map<String, ServiceParameterCache> PARAMETER_TREE_CACHE = new ConcurrentHashMap<>();

    private final static Map<String, AttributeConverter<?, ?>> converters = Map.of("PersonTypeConverter", new PersonTypeConverter());

    public void clearCache() {
        PARAMETER_TREE_CACHE.clear();
    }

    public ServiceParameterCache getParametersCache(Service service) {
        return PARAMETER_TREE_CACHE
                .computeIfAbsent(
                        getCacheKey(service),
                        serviceCode -> createParameterTreeCache(service)
                );
    }

    private String getCacheKey(Service service) {
        if (service.isProxy()) {
            return service.getTargetProxyCode();
        }
        return service.getCode();
    }

    private ServiceParameterCache createParameterTreeCache(Service service) {
        ServiceParameterCache parameterTree = new ServiceParameterCache();
        List<Parameter> parameters = service.getParameters();
        parameterTree.setResponseCache(createResponseCache(service));
        parameterTree.setService(service);
        parameterTree.setRequestBodyNode(createBodyParameterNode(filterByActionType(parameters, ParameterActionType.REQUEST_BODY)));
        parameterTree.setRequestHeaderVariableNode(createLineaerParameterNode(filterByActionType(parameters, ParameterActionType.REQUEST_HEADER)));
        parameterTree.setRequestPathVariableNode(createLineaerParameterNode(filterByActionType(parameters, ParameterActionType.REQUEST_PATH_VARIABLE)));
        parameterTree.setRequestQueryStringVariableNode(createLineaerParameterNode(filterByActionType(parameters, ParameterActionType.REQUEST_QUERY_STRING)));
        parameterTree.setResponseHeaderVariableNode(createLineaerParameterNode(filterByActionType(parameters, ParameterActionType.RESPONSE_HEADER)));
        parameterTree.setConfig(createLineaerParameterNode(filterByActionType(parameters, ParameterActionType.CONFIG)));
        return parameterTree;
    }

    private ResponseCache createResponseCache(Service service) {
        if (service instanceof RestExternalService restExternalService) {
            List<Response> responseConditions = restExternalService.getResponseList();
            ResponseCache conditionCache = createResponseCache(responseConditions);
            List<Response> providerConditions = restExternalService.getServiceProvider().getResponseConditions();
            conditionCache.setProviderConditionCache(createResponseCache(providerConditions));
            return conditionCache;
        }
        return null;
    }

    private ResponseCache createResponseCache(List<Response> responseConditions) {
        ResponseCache conditionCache = new ResponseCache();
        conditionCache.setConditions(responseConditions);
        Map<String, ParameterNode> responseBodyNodeMap = new HashMap<>();
        responseConditions.forEach(responseCondition -> {
            String id = responseCondition.getId();
            ParameterNode node = createBodyParameterNode(responseCondition.getResponseParameters());
            responseBodyNodeMap.put(id, node);
        });
        conditionCache.setResponseBodyNodes(responseBodyNodeMap);
        return conditionCache;
    }

    private ParameterNode createLineaerParameterNode(List<Parameter> requestHeaders) {
        ParameterNode node = new ParameterNode();
        List<ParameterNode> children = new ArrayList<>();
        requestHeaders
                .stream()
                .map(parameter -> new ParameterNode()
                        .setName(parameter.getName())
                        .setValue(parameter)).forEach(children::add);
        node.setNextNodes(children);
        return node;
    }

    private ParameterNode createBodyParameterNode(List<Parameter> parameters) {
        if (Objects.isNull(parameters) || parameters.isEmpty()) {
            return null;
        }
        Parameter rootParent = findRootParent(parameters);
        ParameterNode rootNode = new ParameterNode()
                .setName(Objects.nonNull(rootParent.getName()) ? rootParent.getName() : null)
                .setValue(rootParent);
        return readParameterNodeTree(rootParent, rootNode, parameters);
    }

    private ParameterNode readParameterNodeTree(Parameter parameter, ParameterNode rootNode, List<Parameter> parameters) {
        String id = parameter.getId();
        List<Parameter> parameterList = findByParentId(parameters, id);
        List<ParameterNode> parameterNodes = new ArrayList<>();
        for (Parameter p : parameterList) {
            ParameterNode node = new ParameterNode()
                    .setName(Objects.nonNull(p.getName()) ? p.getName() : null)
                    .setValue(p);
            parameterNodes.add(readParameterNodeTree(p, node, parameters));
        }
        rootNode.setNextNodes(parameterNodes);
        return rootNode;
    }

    public String writeBodyValue(ParameterNode node, Message message, ParameterHandler parameterHandler) {
        Object result = traverseTree(node, message, parameterHandler);
        if (result instanceof JSONObject jsonObject) {
            return jsonObject.toJSONString();
        } else if (result instanceof JSONArray jsonArray) {
            return jsonArray.toJSONString();
        }
        return null;
    }


    @SuppressWarnings("unchecked")
    private Object traverseTree(ParameterNode node, Message message, ParameterHandler parameterHandler) {
        JSONObject result = new JSONObject();
        List<ParameterNode> childNodes = node.getNextNodes();
        childNodes.forEach(childNode -> {
            String name = childNode.getName();
            Parameter value = childNode.getValue();
            ParameterType type = value.getType();
            switch (type) {
                case ARRAY -> traverseArrayNode(childNode, parameterHandler, message, result);
                case OBJECT -> traverseObjectNode(childNode, parameterHandler, message, result);
                case STRING -> result.put(name, extractTextValue(parameterHandler, message, childNode));
                case BOOLEAN -> result.put(name, extractBooleanValue(parameterHandler, message, childNode));
                case NUMBER -> result.put(name, extractNumberValue(parameterHandler, message, childNode));
            }
        });
        if (Objects.nonNull(result.get(null))) {
            return result.get(null);
        }
        return result;
    }

    private Object extractTextValue(ParameterHandler parameterHandler, Message message, ParameterNode childNode) {
        Object textValue = parameterHandler.apply(message, childNode.getValue()).orElse(childNode.getValue().getDefaultValue());
        Object returnValue = (Objects.isNull(textValue)) ? null : StringUtils.cleanUpJsonCharacters(String.valueOf(textValue), false);
        return convert(childNode.getValue().getDatasource().getConvertorCode(), returnValue);
    }

    private Object extractBooleanValue(ParameterHandler parameterHandler, Message message, ParameterNode childNode) {
        String booleanValue = parameterHandler.apply(message, childNode.getValue()).map(String::valueOf).orElse(childNode.getValue().getDefaultValue());
        if (Objects.isNull(booleanValue)) {
            return null;
        }
        if (StringUtils.isNumeric(booleanValue)) {
            return "1".equals(booleanValue);
        }
        return Boolean.parseBoolean(booleanValue);
    }

    private Object extractNumberValue(ParameterHandler parameterHandler, Message message, ParameterNode childNode) {
        return parameterHandler.apply(message, childNode.getValue())
                .map(value -> {
                    if (value instanceof NumericNode numericNode) {
                        return numericNode.numberValue();
                    } else {
                        String numberValue = Optional.ofNullable(String.valueOf(value)).orElse(childNode.getValue().getDefaultValue());
                        if (Objects.isNull(numberValue) || "null".equalsIgnoreCase(numberValue)) {
                            return null;
                        }
                        if (numberValue.contains(".")) {
                            return Double.parseDouble(numberValue);
                        }
                        return Long.parseLong(numberValue);
                    }
                })
                .map(number -> convert(childNode.getValue().getDatasource().getConvertorCode(), number))
                .orElse(null);

    }

    @SuppressWarnings("unchecked")
    private void traverseObjectNode(ParameterNode childNode, ParameterHandler parameterHandler, Message message, JSONObject result) {
        String name = childNode.getName();
        Parameter value = childNode.getValue();
        JSONObject jsonObject = new JSONObject();
        List<ParameterNode> nextNodes = childNode.getNextNodes();
        parameterHandler.apply(message, value).ifPresentOrElse(arrayNodeObject -> {
            ((ArrayNode) arrayNodeObject).forEach(jsonNode -> {
                Message wrapMessageNode = Message.builder().payload(jsonNode).build();
                nextNodes.forEach(nextNode -> jsonObject.put(nextNode.getName(), traverseTree(nextNode, wrapMessageNode, parameterHandler))); //RECURSIVE
                result.put(name, jsonObject);
            });
        }, () -> {
            nextNodes.forEach(nextNode -> jsonObject.put(nextNode.getName(), traverseTree(nextNode, message, parameterHandler))); //RECURSIVE
            result.put(name, jsonObject);
        });
    }

    @SuppressWarnings("unchecked")
    private void traverseArrayNode(ParameterNode childNode, ParameterHandler parameterHandler, Message message, JSONObject result) {
        String name = childNode.getName();
        Parameter value = childNode.getValue();
        JSONArray jsonArray = new JSONArray();
        List<ParameterNode> nextNodes = childNode.getNextNodes();
        parameterHandler.apply(message, value).ifPresentOrElse(arrayNodeObject -> {
            ((ArrayNode) arrayNodeObject).forEach(jsonNode -> {
                Message wrapMessageNode = Message.builder().payload(jsonNode).build();
                nextNodes.forEach(nextNode -> jsonArray.add(traverseTree(nextNode, wrapMessageNode, parameterHandler))); //RECURSIVE
                result.put(name, jsonArray);
            });
        }, () -> {
            nextNodes.forEach(nextNode -> jsonArray.add(traverseTree(nextNode, message, parameterHandler))); //RECURSIVE
            result.put(name, jsonArray);
        });
    }


    private Parameter findRootParent(List<Parameter> parameters) {
        return parameters
                .stream()
                .filter(parameter -> Objects.isNull(parameter.getParent()))
                .findFirst().orElse(null);
    }

    private List<Parameter> findByParentId(List<Parameter> parameters, String parentId) {
        return parameters
                .stream()
                .filter(parameter -> Objects.nonNull(parameter.getParent()))
                .filter(parameter -> parameter.getParent().getId().equals(parentId))
                .toList();
    }

    private List<Parameter> findByOrder(List<Parameter> parameters, int order) {
        return parameters
                .stream()
                .filter(parameter -> Objects.nonNull(parameter.getOrder()))
                .filter(parameter -> parameter.getParent().getOrder().equals(order))
                .toList();
    }

    private int getMaxOrder(List<Parameter> parameters) {
        return parameters
                .stream()
                .map(Parameter::getOrder).max(Comparator.naturalOrder())
                .orElse(-1);
    }

    public void writeHeadersVariable(ParameterNode node, Map<String, Object> headerMap, Message message, ParameterHandler parameterHandler) {
        if (Objects.nonNull(node) && !node.getNextNodes().isEmpty()) {
            node
                    .getNextNodes()
                    .stream()
                    .map(ParameterNode::getValue)
                    .forEach(parameter -> {
                        String headerName = parameter.getName();
                        Object headerValue = parameterHandler.apply(message, parameter).orElse(null);
                        headerMap.put(headerName, headerValue);
                    });
        }
    }

    public String writeRequestPathVariable(String targetUrl, ParameterNode node, Message message, ParameterHandler parameterHandler) {
        if (Objects.nonNull(node) && !node.getNextNodes().isEmpty()) {
            for (ParameterNode childNode : node.getNextNodes()) {
                Parameter parameter = childNode.getValue();
                String pathVariableName = parameter.getName();
                String value = parameterHandler.apply(message, parameter).map(String::valueOf).orElse("");
                value = value.replace("\"", "");
                targetUrl = targetUrl.replace("{" + pathVariableName + "}", value);
            }
        }
        return targetUrl;
    }

    public String getRequestQueryStringVariable(ParameterNode node, Message message, ParameterHandler parameterHandler) {
        if (Objects.nonNull(node) && !node.getNextNodes().isEmpty()) {
            StringBuilder queryString = new StringBuilder("?");
            for (ParameterNode childNode : node.getNextNodes()) {
                Parameter parameter = childNode.getValue();
                String queryVariableName = parameter.getName();
                String value = parameterHandler.apply(message, parameter).map(String::valueOf).orElse("");
                value = value.replace("\"", "");
                queryString.append(queryVariableName).append("=").append(value).append("&");
            }
            return queryString.toString();
        } else {
            return null;
        }
    }

    public List<Parameter> filterByActionType(List<Parameter> parameters, ParameterActionType actionType) {
        return parameters
                .stream()
                .filter(parameter -> actionType.equals(parameter.getActionType()))
                .toList();

    }

    @SuppressWarnings("unchecked")
    private Object convert(String convertorCode, Object value) {
        if (StringUtils.isNotEmpty(convertorCode)) {
            if (!converters.containsKey(convertorCode)) {
                throw new IllegalArgumentException("Unknown convertor code: " + convertorCode);
            }
            AttributeConverter<Object, Object> attributeConverter = (AttributeConverter<Object, Object>) converters.get(convertorCode);
            Object converted = attributeConverter.convertToEntityAttribute(value);
            if (converted instanceof String text) {
                return text;
            } else if (converted instanceof Number number) {
                return number;
            } else if (converted instanceof Boolean bool) {
                return bool;
            } else {
                return String.valueOf(converted);
            }
        }
        return value;
    }

    @FunctionalInterface
    public interface ParameterHandler {
        Optional<Object> apply(Message message, Parameter parameter);
    }

    @Data
    public static class ServiceParameterCache {
        private Service service;
        private ResponseCache responseCache;
        private ParameterNode requestBodyNode;
        private ParameterNode requestPathVariableNode;
        private ParameterNode requestQueryStringVariableNode;
        private ParameterNode requestHeaderVariableNode;
        private ParameterNode responseHeaderVariableNode;
        private ParameterNode config;

    }

    @Getter
    @Setter
    @Accessors(chain = true)
    public static class ResponseCache {
        private List<Response> conditions;
        private ResponseCache providerConditionCache;
        private Map<String, ParameterNode> responseBodyNodes;

        public ParameterNode getResponseBodyNode(Response responseCondition) {
            return responseBodyNodes.get(responseCondition.getId());
        }

        public ParameterNode getResponseBodyNode(String responseConditionId) {
            return responseBodyNodes.get(responseConditionId);
        }
    }

}
