package ir.daneshrefah.scm.core.integration.template.engine;

import ir.daneshrefah.scm.common.model.template.TemplateEngineType;

import java.util.Map;

public class GroovyEngine implements TemplateEngine{
    @Override
    public TemplateEngineType getTemplateEngineType() {
        return  TemplateEngineType.GROOVY;
    }

    @Override
    public String render(String name, String templateText, Map<String, Object> context) throws Exception {
        // transfer rq & rs of nab message
        return "";
    }
}
