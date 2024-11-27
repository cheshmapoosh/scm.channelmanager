package ir.daneshrefah.scm.core.integration.service;

import com.fasterxml.jackson.databind.JsonNode;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.service.ServiceService;
import ir.daneshrefah.scm.plugin.api.integration.MessageGenerator;
import ir.daneshrefah.scm.plugin.api.integration.ServiceProducerTemplate;
import ir.daneshrefah.scm.plugin.api.model.service.external.ProxyService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.camel.model.ProcessorDefinition;
import org.springframework.stereotype.Service;

import java.util.Objects;

@RequiredArgsConstructor
@Service
public class ProxyServiceExecutor extends ServiceExecutor {

    private final ServiceProducerTemplate serviceProducerTemplate;
    private final ServiceService serviceService;

    @Override
    protected void defineServiceRoute(ir.daneshrefah.scm.common.model.service.Service service, ProcessorDefinition<?> processorDefinition) {
        processorDefinition.process(exchange -> {
            Message message = exchange.getMessage().getBody(Message.class);
            JsonNode response = executeProxyService(message);
            message.payload(response);
        });
    }

    @SneakyThrows
    private JsonNode executeProxyService(Message message) {
        ProxyService proxyService = (ProxyService) message.getHeader().getService();
        Message result = callProxyService(proxyService, message);
        if (Objects.nonNull(result.getErrors()) && !result.getErrors().isEmpty()) {
            result.getErrors().forEach(message::addError);
            return objectMapper.readTree(objectMapper.writeValueAsBytes(message.getPayload()));
        }
        return result.getPayload();
    }

    private Message callProxyService(ProxyService service, Message message) {
        ir.daneshrefah.scm.common.model.service.Service targetService = service.getTargetService();
        if (Objects.isNull(targetService)) {
            ir.daneshrefah.scm.common.model.service.Service proxyService =
                    serviceService.findProxyService(service.getId()).orElseThrow(RuntimeException::new);
            targetService = ((ProxyService) proxyService).getTargetService();
            service.setProxyServiceCode(((ProxyService) proxyService).getProxyServiceCode());
        }
        Message tempMessage = MessageGenerator.getInstance().generateInternalMessage(targetService, message.getPayload());
        return serviceProducerTemplate.callService(service.getProxyServiceCode(), tempMessage);
    }


}
