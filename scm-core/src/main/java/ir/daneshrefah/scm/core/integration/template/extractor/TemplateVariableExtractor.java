package ir.daneshrefah.scm.core.integration.template.extractor;

import ir.daneshrefah.scm.common.model.template.TemplateEngineType;

import java.util.Set;

public interface TemplateVariableExtractor {
    TemplateEngineType getTemplateEngineType();
    Set<String> extractVariables(String template);
}