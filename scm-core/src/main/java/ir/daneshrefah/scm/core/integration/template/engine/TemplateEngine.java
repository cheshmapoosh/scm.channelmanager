package ir.daneshrefah.scm.core.integration.template.engine;

import ir.daneshrefah.scm.common.model.template.TemplateEngineType;

import java.util.Map;

public interface TemplateEngine {
    TemplateEngineType getTemplateEngineType();
    String render(String name, String templateText, Map<String, Object> context) throws Exception;
}