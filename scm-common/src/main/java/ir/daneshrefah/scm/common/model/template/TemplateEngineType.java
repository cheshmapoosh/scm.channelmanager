package ir.daneshrefah.scm.common.model.template;

public enum TemplateEngineType {
    FREEMARKER("freemarker"),
    THYMELEAF("thymeleaf"),
    PLACEHOLDER("placeholder"),
    CONSTANT("constant"),
    GROOVY("groovy"),
    DATA_SONNET("datasonnet");

    private  final String type;

    TemplateEngineType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
