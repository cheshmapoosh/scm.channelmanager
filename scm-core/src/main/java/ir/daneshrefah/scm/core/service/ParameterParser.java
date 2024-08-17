package ir.daneshrefah.scm.core.service;

import com.fasterxml.jackson.databind.node.ArrayNode;
import ir.daneshrefah.scm.common.model.dynamic.rest.ParameterNode;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.service.parameter.Parameter;
import ir.daneshrefah.scm.common.model.service.parameter.ParameterType;
import ir.daneshrefah.scm.common.model.service.parameter.ResponseCondition;
import ir.daneshrefah.scm.plugin.api.model.service.external.rest.RestExternalService;
import ir.daneshrefah.scm.utils.string.StringUtils;
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

    private static final Map<String, RestExternalServiceParameterCache> PARAMETER_TREE_CACHE = new ConcurrentHashMap<>();


    public RestExternalServiceParameterCache getParametersCache(RestExternalService restExternalService) {
        return PARAMETER_TREE_CACHE
                .computeIfAbsent(
                        restExternalService.getCode(),
                        serviceCode -> createParameterTreeCache(restExternalService)
                );
    }

    private RestExternalServiceParameterCache createParameterTreeCache(RestExternalService service) {
        RestExternalServiceParameterCache parameterTree = new RestExternalServiceParameterCache();
        List<ResponseCondition> responseConditions = service.getResponseConditions();
        parameterTree.setExternalService(service);
        parameterTree.setRequestBodyNode(createBodyParameterNode(service.getRequestBody()));
        parameterTree.setConditionCache(createConditionCache(service,responseConditions));
        parameterTree.setRequestHeaderVariableNode(createLineaerParameterNode(service.getRequestHeaders()));
        parameterTree.setRequestPathVariableNode(createLineaerParameterNode(service.getRequestPathVariables()));
        parameterTree.setRequestQueryStringVariableNode(createLineaerParameterNode(service.getRequestQueryStringVariables()));
        parameterTree.setResponseHeaderVariableNode(createLineaerParameterNode(service.getResponseHeaders()));
        return parameterTree;
    }

    private ConditionCache createConditionCache(RestExternalService service,List<ResponseCondition> responseConditions) {
        ConditionCache conditionCache = createConditionCache(responseConditions);
        List<ResponseCondition> providerConditions = service.getServiceProvider().getResponseConditions();
        conditionCache.setProviderConditionCache(createConditionCache(providerConditions));
        return conditionCache;
    }

    private ConditionCache createConditionCache(List<ResponseCondition> responseConditions) {
        ConditionCache conditionCache =new ConditionCache();
        conditionCache.setConditions(responseConditions);
        Map<Long, ParameterNode> responseBodyNodeMap = new HashMap<>();
        responseConditions.forEach(responseCondition -> {
            Long id = responseCondition.getId();
            ParameterNode node = createBodyParameterNode(responseCondition.getResponseParameters());
            responseBodyNodeMap.put(id,node);
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
        Long id = parameter.getId();
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
        return (Objects.isNull(textValue)) ? null : StringUtils.cleanUpJsonCharacters(String.valueOf(textValue), false);
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
        String numberValue = String.valueOf(parameterHandler.apply(message, childNode.getValue()).map(String::valueOf).orElse(childNode.getValue().getDefaultValue()));
        if (Objects.isNull(numberValue)) {
            return null;
        }
        if (numberValue.contains(".")) {
            return Double.parseDouble(numberValue);
        }
        return Long.parseLong(numberValue);
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

    private List<Parameter> findByParentId(List<Parameter> parameters, Long parentId) {
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
                targetUrl = targetUrl.replace("{" + pathVariableName + "}", value);
            }
        }
        return targetUrl;
    }

    public String writeRequestQueryStringVariable(String targetUrl, ParameterNode node, Message message, ParameterHandler parameterHandler) {
        if (Objects.nonNull(node) && !node.getNextNodes().isEmpty()) {
            StringBuilder targetUrlBuilder = new StringBuilder(targetUrl + "?");
            for (ParameterNode childNode : node.getNextNodes()) {
                Parameter parameter = childNode.getValue();
                String queryVariableName = parameter.getName();
                String value = parameterHandler.apply(message, parameter).map(String::valueOf).orElse("");
                targetUrlBuilder.append(queryVariableName).append("=").append(value).append("&");
            }
            targetUrl = targetUrlBuilder.toString();
        }
        return targetUrl;
    }


    @FunctionalInterface
    public interface ParameterHandler {
        Optional<Object> apply(Message message, Parameter parameter);
    }

    @Data
    public static class RestExternalServiceParameterCache {
        private RestExternalService externalService;
        private ConditionCache conditionCache;
        private ParameterNode requestBodyNode;
        private ParameterNode requestPathVariableNode;
        private ParameterNode requestQueryStringVariableNode;
        private ParameterNode requestHeaderVariableNode;
        private ParameterNode responseHeaderVariableNode;

    }


    @Getter
    @Setter
    @Accessors(chain = true)
    public static class ConditionCache {
        private List<ResponseCondition> conditions;
        private ConditionCache providerConditionCache;
        private Map<Long,ParameterNode> responseBodyNodes;

        public ParameterNode getResponseBodyNode(ResponseCondition responseCondition){
            return responseBodyNodes.get(responseCondition.getId());
        }
    }

}
