package ir.daneshrefah.scm.docs.client.autoconfigure;

import ir.daneshrefah.scm.docs.client.model.ScmDocCategory;
import ir.daneshrefah.scm.docs.client.model.ScmDocType;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@ConfigurationProperties(prefix = "scm.docs")
public class ScmDocsProperties {

    private boolean enabled = true;

    private String basePath = "/docs";

    private String moduleCode = "scm";

    private final Map<String, String> title = new LinkedHashMap<>(Map.of("en", "SCM Documentation"));

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

    public String getModuleCode() {
        return moduleCode;
    }

    public void setModuleCode(String moduleCode) {
        this.moduleCode = moduleCode;
    }

    public Map<String, String> getTitle() {
        return title;
    }

    public void setTitle(Map<String, String> title) {
        this.title.clear();
        if (title != null) {
            this.title.putAll(title);
        }
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

        private String moduleCode;

        private String serviceCode;

        private String version;

        private ScmDocCategory category = ScmDocCategory.GUIDE;

        private ScmDocType type = ScmDocType.MARKDOWN;

        private final Map<String, String> title = new LinkedHashMap<>();

        private final Map<String, String> description = new LinkedHashMap<>();

        private String mediaType;

        private String fileName;

        private String href;

        private int order;

        private String classpathLocation;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getModuleCode() {
            return moduleCode;
        }

        public void setModuleCode(String moduleCode) {
            this.moduleCode = moduleCode;
        }

        public String getServiceCode() {
            return serviceCode;
        }

        public void setServiceCode(String serviceCode) {
            this.serviceCode = serviceCode;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public ScmDocCategory getCategory() {
            return category;
        }

        public void setCategory(ScmDocCategory category) {
            this.category = category;
        }

        public ScmDocType getType() {
            return type;
        }

        public void setType(ScmDocType type) {
            this.type = type;
        }

        public Map<String, String> getTitle() {
            return title;
        }

        public void setTitle(Map<String, String> title) {
            this.title.clear();
            if (title != null) {
                this.title.putAll(title);
            }
        }

        public Map<String, String> getDescription() {
            return description;
        }

        public void setDescription(Map<String, String> description) {
            this.description.clear();
            if (description != null) {
                this.description.putAll(description);
            }
        }

        public String getMediaType() {
            return mediaType;
        }

        public void setMediaType(String mediaType) {
            this.mediaType = mediaType;
        }

        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        public String getHref() {
            return href;
        }

        public void setHref(String href) {
            this.href = href;
        }

        public int getOrder() {
            return order;
        }

        public void setOrder(int order) {
            this.order = order;
        }

        public String getClasspathLocation() {
            return classpathLocation;
        }

        public void setClasspathLocation(String classpathLocation) {
            this.classpathLocation = classpathLocation;
        }
    }
}
