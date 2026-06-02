package ir.daneshrefah.scm.docs.client.model;

public enum ScmDocType {
    MARKDOWN("text/markdown", ".md"),
    HTML("text/html", ".html"),
    TEXT("text/plain", ".txt"),
    JSON("application/json", ".json");

    private final String contentType;

    private final String defaultExtension;

    ScmDocType(String contentType, String defaultExtension) {
        this.contentType = contentType;
        this.defaultExtension = defaultExtension;
    }

    public String getContentType() {
        return contentType;
    }

    public String getDefaultExtension() {
        return defaultExtension;
    }
}
