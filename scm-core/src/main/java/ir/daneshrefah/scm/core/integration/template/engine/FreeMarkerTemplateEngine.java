package ir.daneshrefah.scm.core.integration.template.engine;

import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import ir.daneshrefah.scm.common.model.template.TemplateEngineType;
import org.springframework.stereotype.Component;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;

@Component("freemarker")
public class FreeMarkerTemplateEngine implements TemplateEngine {
    private final Configuration cfg;

    public FreeMarkerTemplateEngine() {
        cfg = new Configuration(Configuration.VERSION_2_3_32);
        cfg.setTemplateLoader(new StringTemplateLoader());
    }

    @Override
    public TemplateEngineType getTemplateEngineType() {
        return TemplateEngineType.FREEMARKER;
    }

    @Override
    public String render(String name, String templateText, Map<String, Object> context) throws Exception {
        Template template = new Template(name, new StringReader(templateText), cfg);
        try (StringWriter writer = new StringWriter()) {
            template.process(context, writer);
            return writer.toString();
        }
    }
}