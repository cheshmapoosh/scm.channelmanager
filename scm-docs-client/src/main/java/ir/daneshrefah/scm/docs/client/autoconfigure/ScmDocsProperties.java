package ir.daneshrefah.scm.docs.client.autoconfigure;

import ir.daneshrefah.scm.docs.client.model.ScmDocCategory;
import ir.daneshrefah.scm.docs.client.model.ScmDocType;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "scm.docs")
public class ScmDocsProperties {

    private boolean enabled = true;

    private String basePath = "/docs";

    private String title = "SCM Documentation";

    private String classpathRoot = "scm-docs";

    private final List<Document> documents = new ArrayList<>();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getBasePath() {
        return basePath;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getClasspathRoot() {
        return classpathRoot;
    }

    public void setClasspathRoot(String classpathRoot) {
        this.classpathRoot = classpathRoot;
    }

    public List<Document> getDocuments() {
        return documents;
    }

    public String normalizedBasePath() {
        if (!StringUtils.hasText(basePath)) {
            return "/docs";
        }
        String normalized = basePath.trim();
        if (!normalized.startsWith("/")) {
            normalized = "/" + normalized;
        }
        while (normalized.length() > 1 && normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }

    public static class Document {

        private String id;

        private String title;

        private String description;

        private ScmDocType type = ScmDocType.MARKDOWN;

        private ScmDocCategory category = ScmDocCategory.GENERAL;

        private String classpathLocation;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public ScmDocType getType() {
            return type;
        }

        public void setType(ScmDocType type) {
            this.type = type;
        }

        public ScmDocCategory getCategory() {
            return category;
        }

        public void setCategory(ScmDocCategory category) {
            this.category = category;
        }

        public String getClasspathLocation() {
            return classpathLocation;
        }

        public void setClasspathLocation(String classpathLocation) {
            this.classpathLocation = classpathLocation;
        }
    }
}
