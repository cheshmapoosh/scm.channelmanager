package ir.daneshrefah.scm.core.integration.plugin;

import ir.daneshrefah.scm.common.model.definition.Definition;
import ir.daneshrefah.scm.common.model.gateway.ServiceOperation;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.plugin.PluginDetail;
import ir.daneshrefah.scm.common.model.plugin.PluginType;
import ir.daneshrefah.scm.common.model.template.TemplateEngineType;
import ir.daneshrefah.scm.common.plugin.PluginHandler;
import ir.daneshrefah.scm.core.integration.template.context.TemplateContextBuilder;
import ir.daneshrefah.scm.core.integration.template.engine.TemplateEngine;
import ir.daneshrefah.scm.core.integration.template.extractor.TemplateVariableExtractor;
import lombok.RequiredArgsConstructor;
import org.apache.camel.Exchange;
import org.apache.camel.builder.Builder;
import org.apache.camel.model.RouteDefinition;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class ServiceOperationTemplateTransformer implements PluginHandler {

    private final List<TemplateEngine> templateEngines;
    private final List<TemplateVariableExtractor> templateVariableExtractors;
    private final TemplateContextBuilder templateContextBuilder;

    @Override
    public PluginType getType() {
        return PluginType.TRANSFORMER;
    }

    @Override
    public void init(RouteDefinition routeDefinition, PluginDetail pluginDetail, Map<String, ?> properties) {
        ServiceOperation serviceOperation = (ServiceOperation) properties.get(Message.SERVICE_OPERATION);
        if (serviceOperation == null) {
            throw new IllegalArgumentException("No service operation found in exchange");
        }
        Definition definition = serviceOperation.getDefinition();
        routeDefinition.setProperty(Message.SERVICE_OPERATION_DEFINITION, Builder.constant(definition));

        TemplateEngineType templateEngineType = definition.getEngine();

        TemplateVariableExtractor templateVariableExtractor = templateVariableExtractors.stream()
                .filter(e -> Objects.equals(e.getTemplateEngineType(), templateEngineType))
                .findFirst()
                .orElseThrow();
        String templateText = definition.getDetails();
        Set<String> variables = templateVariableExtractor.extractVariables(templateText);
        routeDefinition.setProperty(Message.TEMPLATE_VARIABLES, Builder.constant(variables));

        TemplateEngine templateEngine = templateEngines.stream()
                .filter(e -> Objects.equals(e.getTemplateEngineType(), templateEngineType))
                .findFirst()
                .orElseThrow();
        routeDefinition.setProperty(Message.TEMPLATE_ENGINE, Builder.constant(templateEngine));


    }

    @Override
    public void handle(Exchange exchange, PluginDetail pluginDetail) throws Exception {
        Set<String> variables = exchange.getProperty(Message.TEMPLATE_VARIABLES, Set.class);
        Map<String, Object> context = templateContextBuilder.buildContext(variables, exchange);

        Definition definition = exchange.getProperty(Message.SERVICE_OPERATION_DEFINITION, Definition.class);
        TemplateEngine templateEngine = exchange.getProperty(Message.TEMPLATE_ENGINE, TemplateEngine.class);
        String templateText = definition.getDetails();
        String body = templateEngine.render(definition.getName(), templateText, context);
        exchange.getIn().setBody(body);
    }
}
