package ir.daneshrefah.scm.docs.client.model;

public enum ScmDocType {
    OPENAPI_JSON("application/json", ".json"),
    WSDL("application/xml", ".wsdl"),
    ISO8583_SCHEMA("application/json", ".json"),
    MARKDOWN("text/markdown; charset=UTF-8", ".md"),
    HTML("text/html; charset=UTF-8", ".html");

    private final String defaultMediaType;

    private final String defaultExtension;

    ScmDocType(String defaultMediaType, String defaultExtension) {
        this.defaultMediaType = defaultMediaType;
        this.defaultExtension = defaultExtension;
    }

    public String getDefaultMediaType() {
        return defaultMediaType;
    }

    public String getContentType() {
        return defaultMediaType;
    }

    public String getDefaultExtension() {
        return defaultExtension;
    }
}
