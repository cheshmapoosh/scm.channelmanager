package ir.daneshrefah.scm.core.integration.inbound.rest.springrest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import ir.daneshrefah.scm.common.model.message.MessageInput;
import ir.daneshrefah.scm.core.integration.inbound.rest.AbstractRestInboundChannelGenerator;
import ir.daneshrefah.scm.plugin.api.inbound.AbstractSpringRestInboundController;
import ir.daneshrefah.scm.plugin.api.integration.ErrorHandlerService;
import ir.daneshrefah.scm.plugin.api.integration.ServiceProducerTemplate;
import ir.daneshrefah.scm.plugin.api.utils.ClassLoader;
import ir.daneshrefah.scm.utils.string.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.util.pattern.PathPatternParser;

import java.lang.reflect.Method;
import java.util.Iterator;

import static ir.daneshrefah.scm.core.integration.inbound.InboundConstants.CHANNEL_METADATA_REST_SPRING_CONTROLLER;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-02
 */
@Slf4j
@Component
@Scope("prototype")
public class SpringRestInboundChanelGenerator extends AbstractRestInboundChannelGenerator {

    private final RequestMappingHandlerMapping handlerMapping;
    private ArrayNode controllersArrayNode;

    public SpringRestInboundChanelGenerator(ObjectMapper objectMapper, RequestMappingHandlerMapping handlerMapping,
                                            ServiceProducerTemplate producerTemplate,
                                            ErrorHandlerService errorHandlerService) {
        super(objectMapper,producerTemplate, errorHandlerService);
        this.handlerMapping = handlerMapping;
    }

    @Override
    public boolean initialize() {
        JsonNode metadata = getMetadata();
        this.controllersArrayNode = (null != metadata.get(CHANNEL_METADATA_REST_SPRING_CONTROLLER) && metadata.get(CHANNEL_METADATA_REST_SPRING_CONTROLLER).isArray()) ?
                (ArrayNode) metadata.get(CHANNEL_METADATA_REST_SPRING_CONTROLLER) : null;
        if (null == controllersArrayNode || controllersArrayNode.size() < 1) {
            log.error("no controllers defined in metadata.");
            return false;
        }

        return true;
    }

    @Override
    public boolean registerEndpoints() {
        Iterator<JsonNode> controllersIterator = controllersArrayNode.elements();
        while (controllersIterator.hasNext()) {
            JsonNode controllerNode = controllersIterator.next();
            String controllerClassName = controllerNode.asText();
            AbstractSpringRestInboundController inboundSpringController = null;
            try {
                inboundSpringController = ClassLoader.findBeanOrCreateInstanceOfClass(
                        controllerClassName, AbstractSpringRestInboundController.class);
                inboundSpringController.setExecutor(this);
            } catch (Exception e) {
                log.error("error on get controller class '{}'", controllerClassName);
            }

            registerControllerMapping(inboundSpringController);
        }
        return true;
    }

    private void registerControllerMapping(AbstractSpringRestInboundController inboundSpringController) {
        if (null == inboundSpringController) {
            return;
        }
        log.info("start register controller '{}'", inboundSpringController.getClass().getSimpleName());
        Class<? extends AbstractSpringRestInboundController> controllerClass = inboundSpringController.getClass();
        RequestMapping controllerRequestMapping = controllerClass.getAnnotation(RequestMapping.class);
        String mappingPrefix = StringUtils.EMPTY;
        if (null != controllerRequestMapping && controllerRequestMapping.path().length > 0) {
            mappingPrefix = controllerRequestMapping.path()[0];
        }
        if (null != controllerRequestMapping && controllerRequestMapping.value().length > 0) {
            mappingPrefix = controllerRequestMapping.value()[0];
        }
        Method[] methods = controllerClass.getDeclaredMethods();
        for (Method method : methods) {
            RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
            GetMapping getMapping = method.getAnnotation(GetMapping.class);
            PostMapping postMapping = method.getAnnotation(PostMapping.class);
            PutMapping putMapping = method.getAnnotation(PutMapping.class);
            DeleteMapping deleteMapping = method.getAnnotation(DeleteMapping.class);
            if (null != requestMapping) {
                registerRequestMapping(inboundSpringController, method, requestMapping, mappingPrefix);
            } else if (null != getMapping) {
                registerGetMapping(inboundSpringController, method, getMapping, mappingPrefix);
            } else if (null != postMapping) {
                registerPostMapping(inboundSpringController, method, postMapping, mappingPrefix);
            } else if (null != putMapping) {
                registerPutMapping(inboundSpringController, method, putMapping, mappingPrefix);
            } else if (null != deleteMapping) {
                registerDeleteMapping(inboundSpringController, method, deleteMapping, mappingPrefix);
            }
        }
    }

    private void registerDeleteMapping(AbstractSpringRestInboundController inboundSpringController, Method method,
                                       DeleteMapping deleteMapping, String mappingPrefix) {
        String[] paths = null != deleteMapping.path() ? deleteMapping.path() : deleteMapping.value();
        registerMapping(inboundSpringController, method, paths,
                new RequestMethod[] {RequestMethod.DELETE}, mappingPrefix);
    }

    private void registerPutMapping(AbstractSpringRestInboundController inboundSpringController, Method method,
                                    PutMapping putMapping, String mappingPrefix) {
        String[] paths = null != putMapping.path() ? putMapping.path() : putMapping.value();
        registerMapping(inboundSpringController, method, paths,
                new RequestMethod[] {RequestMethod.PUT}, mappingPrefix);
    }

    private void registerPostMapping(AbstractSpringRestInboundController inboundSpringController, Method method,
                                     PostMapping postMapping, String mappingPrefix) {
        String[] paths = null != postMapping.path() ? postMapping.path() : postMapping.value();
        registerMapping(inboundSpringController, method, paths,
                new RequestMethod[] {RequestMethod.POST}, mappingPrefix);
    }

    private void registerGetMapping(AbstractSpringRestInboundController inboundSpringController, Method method,
                                    GetMapping getMapping, String mappingPrefix) {
        String[] paths = null != getMapping.path() ? getMapping.path() : getMapping.value();
        registerMapping(inboundSpringController, method, paths,
                new RequestMethod[] {RequestMethod.GET}, mappingPrefix);
    }

    private void registerRequestMapping(AbstractSpringRestInboundController inboundSpringController, Method method,
                                        RequestMapping requestMapping, String mappingPrefix) {
        String[] paths = null != requestMapping.path() ? requestMapping.path() : requestMapping.value();
        registerMapping(inboundSpringController, method, paths,
                requestMapping.method(), mappingPrefix);
    }

    private void registerMapping(AbstractSpringRestInboundController inboundSpringController, Method method,
                                 String[] paths, RequestMethod[] requestMethod, String mappingPrefix) {
        RequestMappingInfo.BuilderConfiguration options = new RequestMappingInfo.BuilderConfiguration();
        options.setPatternParser(new PathPatternParser());
        String path = null != mappingPrefix ? mappingPrefix : StringUtils.EMPTY;
        if (paths.length > 0) {
            path = path + paths[0];
        }
        log.info("register request mapping '{}:{}' for class '{}:{}'", requestMethod, path,
                inboundSpringController.getClass().getSimpleName(), method.getName());
        RequestMappingInfo info = RequestMappingInfo
                .paths(path)
                .methods(requestMethod)
                .options(options)
                .build();
        handlerMapping.registerMapping(
                info,
                inboundSpringController,
                method);
    }

}
