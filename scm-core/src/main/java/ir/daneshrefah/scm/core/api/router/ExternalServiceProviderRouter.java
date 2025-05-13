package ir.daneshrefah.scm.core.api.router;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.service.AbstractAuditableExternalServiceProvider;
import ir.daneshrefah.scm.common.model.service.ServiceProviderProtocol;
import ir.daneshrefah.scm.common.model.transformer.TransformerRelation;
import ir.daneshrefah.scm.common.model.transformer.TransformerRelationType;
import ir.daneshrefah.scm.common.service.ServiceService;
import ir.daneshrefah.scm.core.integration.provider.DefaultRestServiceProviderExecutor;
import ir.daneshrefah.scm.plugin.api.model.service.external.ExternalServiceProviderExecutor;
import ir.daneshrefah.scm.plugin.api.model.service.external.povider.executor.AbstractBaseExternalServiceProviderExecutor;
import ir.daneshrefah.scm.plugin.api.service.TransformerService;
import ir.daneshrefah.scm.plugin.api.transformer.TransformerExecutionWrapper;
import ir.daneshrefah.scm.plugin.api.utils.ClassLoader;
import lombok.RequiredArgsConstructor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.RouteDefinition;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static ir.daneshrefah.scm.plugin.api.model.service.external.ExternalServiceProviderExecutor.*;

@RequiredArgsConstructor
@Component
public class ExternalServiceProviderRouter extends RouteBuilder {
    private final static Logger LOGGER = LoggerFactory.getLogger(ExternalServiceProviderRouter.class);

    private final ServiceService serviceService;
    private final TransformerService transformerService;
    private final ObjectMapper objectMapper;
    private final ObjectFactory<DefaultRestServiceProviderExecutor> restServiceProviderExecutorFactory;

    @Override
    public void configure() {
        List<AbstractAuditableExternalServiceProvider> providers = serviceService.findServiceProviderList();
        if (CollectionUtils.isEmpty(providers)) {
            LOGGER.warn("No service provider found");
        }
        providers.forEach(provider -> {
            RouteDefinition routeDefinition = from("direct:" + uri(provider)).routeId(routeId(provider));
            List<TransformerRelation> transformerRelations = transformerService.findAllTransformerRelationsBySource(provider.getId());

            routeDefinition.process(exchange -> {
                Message message = exchange.getMessage().getBody(Message.class);
                exchange.setProperty(HEADER_ORIGINAL_MESSAGE, message);
                Object requestBody = transformRequest(
                        prepareTransformerExecutionWrapper(transformerRelations, TransformerRelationType.SERVICE_PROVIDER_REQUEST), message);
                exchange.getMessage().setBody(requestBody);
            });
            ExternalServiceProviderExecutor externalServiceProviderExecutor = extractServiceProviderExecutorInstance(provider);
            if (Objects.isNull(externalServiceProviderExecutor)) {
                return;
            }
            externalServiceProviderExecutor.endpointCallRouteDefinition(routeDefinition);
            routeDefinition.process(exchange -> {
                exchange.setProperty(HEADER_END_TIME, Instant.now());
                String response = exchange.getMessage().getBody(String.class);
                exchange.setProperty(HEADER_RESPONSE_BODY, response);
                JsonNode jsonResponse = null;
                try {
                    jsonResponse = objectMapper.readTree(response);
                } catch (JsonProcessingException e) {
                    jsonResponse = objectMapper.valueToTree(response);
                }
                if (null != exchange.getException()) {
                    return;
                }
                Message message = exchange.getProperty(HEADER_ORIGINAL_MESSAGE, Message.class);
                message.payload(transformResponse(
                        prepareTransformerExecutionWrapper(transformerRelations, TransformerRelationType.SERVICE_PROVIDER_RESPONSE), message, jsonResponse));
                exchange.getMessage().setBody(message);
            });
            routeDefinition.end();
        });
    }

    private String uri(AbstractAuditableExternalServiceProvider provider) {
        return "ESP_" + provider.getCode();
    }

    private String routeId(AbstractAuditableExternalServiceProvider provider) {
        return "ROUTE_" + uri(provider);
    }

    public final JsonNode transformRequest(List<TransformerExecutionWrapper> transformerRelations, Message message) {
        JsonNode payload = message.getPayload();
        if (Objects.isNull(transformerRelations) || transformerRelations.isEmpty()) {
            return payload;
        }
        for (TransformerExecutionWrapper transformerExecutionWrapper : transformerRelations) {
            payload = transformerExecutionWrapper.getTransformerInstance()
                    .transform(payload, message, transformerExecutionWrapper.getTransformerRelation().getMetadata());

        }
        return payload;
    }

    public JsonNode transformResponse(List<TransformerExecutionWrapper> transformerRelations, Message message, JsonNode payload) {
        if (Objects.isNull(transformerRelations) || transformerRelations.isEmpty()) {
            return payload;
        }
        for (TransformerExecutionWrapper transformerExecutionWrapper : transformerRelations) {
            payload = transformerExecutionWrapper.getTransformerInstance()
                    .transform(payload, message, transformerExecutionWrapper.getTransformerRelation().getMetadata());
        }
        return payload;
    }


    protected final List<TransformerExecutionWrapper> prepareTransformerExecutionWrapper(List<TransformerRelation> transformerRelations,
                                                                                         TransformerRelationType filter) {
        if (Objects.isNull(transformerRelations) || transformerRelations.isEmpty()) {
            return Collections.emptyList();
        }
        return transformerRelations.stream()
                .filter(t -> null == filter || filter.equals(t.getRelationType()))
                .map(TransformerExecutionWrapper::new)
                .collect(Collectors.toList());
    }


    private ExternalServiceProviderExecutor extractServiceProviderExecutorInstance(AbstractAuditableExternalServiceProvider serviceProviderModel) {
        try {
            ExternalServiceProviderExecutor provider = null;
            if (ServiceProviderProtocol.REST.equals(serviceProviderModel.getProtocol())) {
                provider = restServiceProviderExecutorFactory.getObject();
            } else if (ServiceProviderProtocol.CUSTOM.equals(serviceProviderModel.getProtocol())) {
                provider = ClassLoader.findBeanOrCreateInstanceOfClass(serviceProviderModel.getProviderClassName(),
                        AbstractBaseExternalServiceProviderExecutor.class, serviceProviderModel);
            }
            if (null == provider) {
                log.warn("error on create instance of '{}' provider with className '{}'", serviceProviderModel.getCode(),
                        serviceProviderModel.getProviderClassName());
                return null;
            }
            provider.init(serviceProviderModel);
            return provider;
        } catch (Exception e) {
            log.warn("error register external service provider: " + serviceProviderModel.getCode(), e);
        }
        return null;
    }

}
