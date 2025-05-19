package ir.daneshrefah.scm.core.integration.template.engine;

import ir.daneshrefah.scm.common.model.template.TemplateEngineType;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.StringTemplateResolver;

import java.util.Map;

@Component("thymeleaf")
public class ThymeleafTemplateEngine implements TemplateEngine {

    private final org.thymeleaf.TemplateEngine engine;

    public ThymeleafTemplateEngine() {
        StringTemplateResolver resolver = new StringTemplateResolver();
        resolver.setTemplateMode("HTML");
        resolver.setCacheable(false); // disable cache for dynamic strings

        this.engine = new org.thymeleaf.TemplateEngine();
        this.engine.setTemplateResolver(resolver);
    }

    @Override
    public TemplateEngineType getTemplateEngineType() {
        return TemplateEngineType.THYMELEAF;
    }

    @Override
    public String render(String name, String templateText, Map<String, Object> contextMap) {
        Context context = new Context();
        context.setVariables(contextMap);
        return engine.process(templateText, context);
    }
}
