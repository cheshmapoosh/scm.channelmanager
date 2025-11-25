package ir.daneshrefah.scm.core.integration.plugin;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import freemarker.cache.StringTemplateLoader;
import freemarker.core.JSONOutputFormat;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import ir.daneshrefah.scm.common.handler.PluginHandler;
import ir.daneshrefah.scm.common.handler.StatusHandler;
import ir.daneshrefah.scm.common.model.definition.Definition;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.operation.Operation;
import ir.daneshrefah.scm.common.model.operation.OperationDefinition;
import ir.daneshrefah.scm.common.model.operation.OperationDefinitionType;
import ir.daneshrefah.scm.common.model.plugin.PluginDetail;
import ir.daneshrefah.scm.common.model.plugin.PluginPhase;
import ir.daneshrefah.scm.common.model.plugin.PluginType;
import ir.daneshrefah.scm.common.model.template.TemplateEngineType;
import ir.daneshrefah.scm.core.integration.template.context.TemplateContextBuilder;
import ir.daneshrefah.scm.core.integration.template.engine.TemplateEngine;
import ir.daneshrefah.scm.core.integration.template.extractor.TemplateVariableExtractor;
import ir.daneshrefah.scm.utils.string.HttpConstants;
import ir.daneshrefah.scm.utils.string.JsonPathFinder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.builder.Builder;
import org.apache.camel.model.RouteDefinition;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class OperationTemplateTransformer implements PluginHandler {

    private final ObjectMapper objectMapper;
    private final List<TemplateEngine> templateEngines;
    private final List<TemplateVariableExtractor> templateVariableExtractors;
    private final TemplateContextBuilder templateContextBuilder;
    private final Map<String, StatusHandler> statusHandlers;


    @Override
    public PluginType getType() {
        return PluginType.TRANSFORMER;
    }

    @Override
    public void init(RouteDefinition routeDefinition, PluginDetail pluginDetail, Map<String, ?> properties) {
        Operation operation = (Operation) properties.get(Message.OPERATION);
        
        if (operation == null) {
            throw new IllegalArgumentException("No service operation found in exchange");
        }
        OperationDefinition operationDefinition = null;

        if (Objects.equals(PluginPhase.BEFORE, pluginDetail.getPhase())) {
            operationDefinition = operation.getDefinitions().stream()
                    .filter(d -> Objects.equals(OperationDefinitionType.REQUEST_TEMPLATE, d.getType()))
                    .findFirst().orElse(null);
        } else if (Objects.equals(PluginPhase.AFTER, pluginDetail.getPhase())) {
            operationDefinition = operation.getDefinitions().stream()
                    .filter(d -> Objects.equals(OperationDefinitionType.RESPONSE_TEMPLATE, d.getType()))
                    .findFirst().orElse(null);
        }
        if (operationDefinition == null) {
            return;
        }

        Definition definition = operationDefinition.getDefinition();
        routeDefinition.setProperty(Message.OPERATION_PHASE_DEFINITION, Builder.constant(definition));

        TemplateEngineType templateEngineType = definition.getEngine();



        if (templateEngineType == TemplateEngineType.GROOVY || templateEngineType == TemplateEngineType.DATA_SONNET) {

            if (pluginDetail.getPhase() == PluginPhase.BEFORE) {
                routeDefinition.transform().language(templateEngineType.getType(),definition.getDetails());
            }
            if (pluginDetail.getPhase() == PluginPhase.AFTER) {
                routeDefinition.transform().language(templateEngineType.getType(),definition.getDetails());
            }

            routeDefinition.setProperty(Message.TEMPLATE_ENGINE,
                    Builder.constant(TemplateEngineType.GROOVY));

            return;
        }

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
        TemplateEngine templateEngine = exchange.getProperty(Message.TEMPLATE_ENGINE, TemplateEngine.class);
        if(templateEngine==null){
            log.warn("No template engine found in exchange, skipping template transformation.");
            return;
        }

        Definition definition = exchange.getProperty(Message.OPERATION_PHASE_DEFINITION, Definition.class);
        if (pluginDetail != null && pluginDetail.getPhase().equals(PluginPhase.BEFORE)) {
            Set<String> variables = exchange.getProperty(Message.TEMPLATE_VARIABLES, Set.class);
            Map<String, Object> context = templateContextBuilder.buildContext(variables, exchange);
            String templateText = definition.getDetails();
            String body = templateEngine.render(definition.getName(), templateText, context);
            log.info("Template engin is [{}] and definition id is [{}] and before plugin rendered  [{}]", templateEngine.getTemplateEngineType(), definition.getId(), body);
            exchange.getIn().setHeader(HttpConstants.HTTP_HEADER_CONTENT_TYPE, HttpConstants.HTTP_HEADER_CONTENT_TYPE_JSON);
            exchange.getIn().setBody(body);
        } else {
            JsonNode response = exchange.getIn().getBody(JsonNode.class);
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> context = mapper.convertValue(response, new TypeReference<>() {});
            String templateText = definition.getDetails();
            String render =  templateEngine.render(definition.getName(), templateText, context);
            log.info("Template engin is [{}] and definition id is [{}] and after plugin rendered  [{}]", templateEngine.getTemplateEngineType(), definition.getId(), render);
            JsonNode jsonNode = mapper.readTree(render);
            String statusHandlerName = JsonPathFinder.defaultAsText(jsonNode, "statusHandler");
            if (statusHandlerName != null) {
                StatusHandler statusHandler = statusHandlers.get(statusHandlerName);
                if (statusHandler != null) {
                    statusHandler.handle(exchange);
                } else {
                    throw new IllegalArgumentException("Unknown status handler " + statusHandlerName);
                }
            }
            exchange.getIn().setBody(JsonPathFinder.defaultNode(jsonNode, "response"));
        }
    }

    public static void main(String[] args) throws IOException, TemplateException {
        String response = "{\"SUBORG\": 13921"+"}";
        ObjectMapper mapper = new ObjectMapper();
        JsonNode res = mapper.readTree(response);
        Map<String, Object> context = mapper.convertValue(res, new TypeReference<Map<String, Object>>() {});
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_32);
        cfg.setTemplateLoader(new StringTemplateLoader());
        cfg.setOutputFormat(JSONOutputFormat.INSTANCE);
        cfg.setNumberFormat("computer");

        Template template = new Template("test", new StringReader("{\"subOrg\":${SUBORG}"), cfg);
        try (StringWriter writer = new StringWriter()) {
            template.process(context, writer);
            System.out.println(writer.toString());
        }


    }
}
