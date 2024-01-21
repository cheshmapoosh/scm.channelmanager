package ir.daneshrefah.scm.core.integration.service;

import com.fasterxml.jackson.databind.JsonNode;
import ir.daneshrefah.scm.common.exception.BaseException;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.plugin.api.exception.JavaServiceExecutionException;
import ir.daneshrefah.scm.plugin.api.model.service.java.JavaService;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;
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
    protected Object executeInternal(ir.daneshrefah.scm.common.model.service.Service service, Message message, Object requestPayload) {
        JavaServiceFinder.MethodInfo methodInfo = findServiceMethodInfo((JavaService) service);
        if (null != methodInfo.getError()) {
            throw methodInfo.getError();
        }

        try {
            Object[] args = prepareMethodArgs(message, service, requestPayload, methodInfo);
            return methodInfo.getMethod().invoke(methodInfo.getInstance(), args);
        } catch (BaseException e) {
            throw e;
        } catch (InvocationTargetException e) {
            if (null != e.getTargetException() && e.getTargetException() instanceof BaseException) {
                throw (BaseException) e.getTargetException();
            }
            throw new JavaServiceExecutionException(e.getTargetException(), (JavaService) service);
        } catch (Exception e) {
            if (e.getCause() instanceof BaseException) {
                throw (BaseException) e.getCause();
            }
            throw new JavaServiceExecutionException(e, (JavaService) service);
        }
    }

    private Object[] prepareMethodArgs(Message message, ir.daneshrefah.scm.common.model.service.Service service,
                                       Object payload, JavaServiceFinder.MethodInfo methodInfo) {
        if (null == methodInfo || null == methodInfo.getParamTypes()) {
            return null;
        }

        Iterator<Map.Entry<String, JsonNode>> fields = message.getPayload().fields();
        Class<?>[] paramTypes = methodInfo.getParamTypes();
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
                result[i] = null != node && !node.isNull() && node.isTextual() ? node.asText() : null;
            } else if (parameterType.equals(Integer.class)) {
                JsonNode node = null != field ? field.getValue() : null;
                result[i] = null != node && !node.isNull() && node.isInt() ? node.asInt() : null;
            } else if (parameterType.equals(Long.class)) {
                JsonNode node = null != field ? field.getValue() : null;
                result[i] = null != node && !node.isNull() && node.isLong() ? node.asLong() : null;
            } else if (parameterType.equals(Message.class)) {
                result[i] = message;
            } else if (parameterType.equals(ir.daneshrefah.scm.common.model.service.Service.class)) {
                result[i] = service;
            } else if (parameterType.equals(Object.class)) {
                result[i] = payload;
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
