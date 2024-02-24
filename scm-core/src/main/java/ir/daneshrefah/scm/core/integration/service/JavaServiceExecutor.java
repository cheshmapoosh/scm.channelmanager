package ir.daneshrefah.scm.core.integration.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import ir.daneshrefah.scm.common.exception.InputMismatchException;
import ir.daneshrefah.scm.common.exception.InvalidInputException;
import ir.daneshrefah.scm.common.exception.InvalidRequestFormatException;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.plugin.api.model.service.java.JavaService;
import ir.daneshrefah.scm.uaa.common.model.authentication.UserAuthentication;
import ir.daneshrefah.scm.uaa.common.utils.AuthenticationUtils;
import ir.daneshrefah.scm.utils.string.StringUtils;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-08-07
 */
@Service
public class JavaServiceExecutor extends ServiceExecutor {

    private Map<String, JavaServiceFinder.MethodInfo> serviceCache = new HashMap<>();

    @Override
    protected JsonNode executeInternal(ir.daneshrefah.scm.common.model.service.Service service, Message message) throws Exception {
        JavaServiceFinder.MethodInfo methodInfo = findServiceMethodInfo((JavaService) service);
        if (null != methodInfo.getError()) {
            throw methodInfo.getError();
        }

//        try {
            Object[] args = prepareMethodArgs(message, service, methodInfo);
            Object response = methodInfo.getMethod().invoke(methodInfo.getInstance(), args);
            if (response instanceof JsonNode) {
                return (JsonNode) response;
            } else if (response instanceof String) {
                return objectMapper.readTree((String) response);
            } else {
                return objectMapper.valueToTree(response);
            }
//        } catch (BaseException e) {
//            throw e;
//        } catch (Exception e) {
//            throw e;
//        } catch (IllegalArgumentException e) {
//            throw new InputMismatchException("serviceMethod", e);
//        } catch (Exception e) {
//            if (e.getCause() instanceof BaseException) {
//                throw (BaseException) e.getCause();
//            }
//            throw new JavaServiceExecutionException(e, (JavaService) service);
//        }
    }

    private Object[] prepareMethodArgs(Message message, ir.daneshrefah.scm.common.model.service.Service service,
                                       JavaServiceFinder.MethodInfo methodInfo) {
        if (null == methodInfo || null == methodInfo.getParamTypes()) {
            return null;
        }

        Iterator<Map.Entry<String, JsonNode>> fields = message.getPayload().fields();
        Class<?>[] paramTypes = methodInfo.getParamTypes();
        /*if (message.getPayload().size() < paramTypes.length) {
            throw new InputMismatchException(paramTypes.length, message.getPayload().size());
        }*/
        Object[] result = new Object[paramTypes.length];
        for (int i = 0; i < paramTypes.length; i++) {
            Map.Entry<String, JsonNode> field = null;
            if (fields.hasNext()) {
                field = fields.next();
            }
            Class parameterType = paramTypes[i];
            if (parameterType.equals(String.class)) {
//                JsonNode node = message.getPayload().get(methodInfo.getMethod().getParameters()[i].getName());
                JsonNode node = null != field ? field.getValue() : null;
                if (!node.isNull() && !node.isTextual()) {
                    throw new InvalidInputException(field.getKey());
                }
                result[i] = null != node && !node.isNull() && node.isTextual() ? node.asText() : null;
            } else if (parameterType.equals(Integer.class)) {
                JsonNode node = null != field ? field.getValue() : null;
                if (!node.isNull() && !node.isInt()) {
                    throw new InvalidInputException(field.getKey());
                }
                result[i] = null != node && !node.isNull() && node.isInt() ? node.asInt() : null;
            } else if (parameterType.equals(boolean.class)) {
                JsonNode node = null != field ? field.getValue() : null;
                if (!node.isNull() && !node.isBoolean()) {
                    throw new InvalidInputException(field.getKey());
                }
                result[i] = null != node && !node.isNull() && node.isBoolean() ? node.asBoolean() : false;
            } else if (parameterType.equals(Boolean.class)) {
                JsonNode node = null != field ? field.getValue() : null;
                if (!node.isNull() && !node.isBoolean()) {
                    throw new InvalidInputException(field.getKey());
                }
                result[i] = null != node && !node.isNull() && node.isBoolean() ? node.asBoolean() : null;
            } else if (parameterType.equals(Long.class)) {
                JsonNode node = null != field ? field.getValue() : null;
                Long value = null != node && !node.isNull() && node.isLong() ? node.asLong() : null;
                if (null == value && !node.isNull() && node.isTextual() && StringUtils.isNumeric(node.asText())) {
                    value = Long.valueOf(node.asText());
                }
                if (!node.isNull() && null == value) {
                    throw new InvalidInputException(field.getKey());
                }
                result[i] = value;
            } else if (parameterType.equals(Message.class)) {
                result[i] = message;
            } else if (parameterType.equals(UserAuthentication.class)) {
                result[i] = AuthenticationUtils.getLoggedInUser(message);
            } else if (parameterType.equals(ir.daneshrefah.scm.common.model.service.Service.class)) {
                result[i] = service;
            } else if (parameterType.equals(Object.class)) {
                result[i] = message.getPayload();
            } else {
                try {
                    result[i] = objectMapper.treeToValue(message.getPayload(), parameterType);
                } catch (JsonProcessingException e) {
                    throw new InvalidRequestFormatException("payload", e);
                }
            }
        }
        return result;
    }

    private JavaServiceFinder.MethodInfo findServiceMethodInfo(JavaService service) {
        if (!serviceCache.containsKey(service.getId())) {
            JavaServiceFinder.MethodInfo methodInfo = JavaServiceFinder.findJavaServiceMethodInfo(service);
            serviceCache.put(service.getId(), methodInfo);
        }
        return serviceCache.get(service.getId());
    }

}
