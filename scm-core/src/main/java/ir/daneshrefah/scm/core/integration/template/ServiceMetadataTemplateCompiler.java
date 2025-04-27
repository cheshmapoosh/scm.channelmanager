package ir.daneshrefah.scm.core.integration.template;

import com.fasterxml.jackson.databind.ObjectMapper;
import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import ir.daneshrefah.scm.common.model.service.parameter.Parameter;
import ir.daneshrefah.scm.plugin.api.service.ParameterDataProvider;

import java.io.IOException;
import java.nio.channels.Selector;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static ir.daneshrefah.scm.core.integration.template.ServiceMetadataTemplate.FREEMARKER;
import static ir.daneshrefah.scm.core.integration.template.ServiceMetadataTemplate.PLACEHOLDER;

public class ServiceMetadataTemplateCompiler {
    private String name;
    private String engine;
    private String template;
    private final Map<String, Supplier<ServiceMetadataTemplate>> templates;
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private List<Parameter> parameters;
    private ParameterDataProvider provider;
    private static final Configuration freemarkerConfig = new Configuration(Configuration.VERSION_2_3_31);

    static {
        freemarkerConfig.setTemplateLoader(new StringTemplateLoader());
    }


    ServiceMetadataTemplateCompiler() {
        Supplier<ServiceMetadataTemplate> freemarkerServiceMetadataTemplateSupplier = () -> {
            try {
                FreemarkerServiceMetadataTemplate freemarkerServiceMetadataTemplate = new FreemarkerServiceMetadataTemplate(name, template, objectMapper, freemarkerConfig);
                freemarkerServiceMetadataTemplate.setParameters(parameters);
                freemarkerServiceMetadataTemplate.setParameterDataProvider(provider);
                return freemarkerServiceMetadataTemplate;
            } catch (IOException e) {
                //TODO SCMNEW-5: handle with scm exception
                throw new RuntimeException(e);
            }
        };
        Supplier<ServiceMetadataTemplate> placeholderServiceMetadataTemplateSupplier = () -> {
            PlaceholderServiceMetadataTemplate placeholderServiceMetadataTemplate = new PlaceholderServiceMetadataTemplate(name, template, new ObjectMapper());
            placeholderServiceMetadataTemplate.setParameters(parameters);
            placeholderServiceMetadataTemplate.setParameterDataProvider(provider);
            return placeholderServiceMetadataTemplate;
        };

        this.templates = Map.of(
                FREEMARKER, freemarkerServiceMetadataTemplateSupplier,
                PLACEHOLDER, placeholderServiceMetadataTemplateSupplier
        );

    }

    public static ServiceMetadataTemplateCompiler compiler() {
        return new ServiceMetadataTemplateCompiler();
    }

    public ServiceMetadataTemplateCompiler name(String name){
        this.name = name;
        return this;
    }
    public ServiceMetadataTemplateCompiler engin(String engine){
        this.engine = engine;
        return this;
    }
    public ServiceMetadataTemplateCompiler template(String template){
        this.template = template;
        return this;
    }
    public ServiceMetadataTemplate compile() {
        Supplier<ServiceMetadataTemplate> template =  templates.get(engine);
        if (template == null){
            //TODO SCMNEW-5: handle with scm exception
            throw new RuntimeException("Template " + engine + " not found");
        }
        return template.get();
    }

    public ServiceMetadataTemplateCompiler parameters(List<Parameter> parameters) {
        this.parameters = parameters;
        return this;
    }

    public ServiceMetadataTemplateCompiler provider(ParameterDataProvider provider) {
        this.provider = provider;
        return this;
    }


}