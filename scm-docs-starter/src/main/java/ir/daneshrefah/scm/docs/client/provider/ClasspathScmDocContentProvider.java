package ir.daneshrefah.scm.docs.client.provider;

import ir.daneshrefah.scm.docs.client.autoconfigure.ScmDocsProperties;
import ir.daneshrefah.scm.docs.client.model.ScmDocContent;
import ir.daneshrefah.scm.docs.client.model.ScmDocDescriptor;
import ir.daneshrefah.scm.docs.client.model.ScmDocType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class ClasspathScmDocContentProvider implements ScmDocCatalogProvider, ScmDocContentProvider {

    private static final Logger log = LoggerFactory.getLogger(ClasspathScmDocContentProvider.class);

    private static final String CLASSPATH_PREFIX = "classpath:";

    private final ScmDocsProperties properties;

    private final ResourceLoader resourceLoader;

    private final Map<String, ScmDocsProperties.Document> documentsById;

    public ClasspathScmDocContentProvider(ScmDocsProperties properties, ResourceLoader resourceLoader) {
        this.properties = properties;
        this.resourceLoader = resourceLoader;
        this.documentsById = indexDocuments(properties);
        log.info("SCM docs classpath catalog initialized configuredDocuments={} indexedDocuments={} classpathRoot={}",
                properties.getDocuments().size(), documentsById.size(), properties.getClasspathRoot());
    }

    @Override
    public Collection<ScmDocDescriptor> findAll() {
        return documentsById.values().stream()
                .map(this::toDescriptor)
                .toList();
    }

    @Override
    public Optional<ScmDocContent> findById(String docId) {
        String safeDocId = requireSafeDocId(docId);
        log.debug("SCM docs classpath lookup started docId={}", safeDocId);
        ScmDocsProperties.Document document = documentsById.get(safeDocId);
        if (document == null) {
            log.debug("SCM docs classpath lookup has no configured document docId={}", safeDocId);
            return Optional.empty();
        }

        String resourceLocation = resolveClasspathLocation(document);
        Resource resource = resourceLoader.getResource(CLASSPATH_PREFIX + resourceLocation);
        if (!resource.exists()) {
            log.warn("SCM docs classpath resource missing docId={} type={} category={} moduleCode={}",
                    safeDocId, document.getType(), document.getCategory(), firstText(document.getModuleCode(), properties.getModuleCode()));
            return Optional.empty();
        }
        if (!resource.isReadable()) {
            log.warn("SCM docs classpath resource unreadable docId={} type={} category={} moduleCode={}",
                    safeDocId, document.getType(), document.getCategory(), firstText(document.getModuleCode(), properties.getModuleCode()));
            return Optional.empty();
        }

        try {
            ScmDocDescriptor descriptor = toDescriptor(document);
            log.debug("SCM docs classpath resource loading docId={} type={} category={} moduleCode={} serviceCode={} version={}",
                    descriptor.id(), descriptor.type(), descriptor.category(), descriptor.moduleCode(),
                    descriptor.serviceCode(), descriptor.version());
            byte[] body;
            try (InputStream inputStream = resource.getInputStream()) {
                body = inputStream.readAllBytes();
            }
            log.debug("SCM docs classpath resource loaded docId={} mediaType={} fileName={}",
                    descriptor.id(), descriptor.mediaType(), descriptor.fileName());
            return Optional.of(new ScmDocContent(
                    descriptor,
                    body,
                    descriptor.mediaType(),
                    descriptor.fileName()
            ));
        } catch (IOException exception) {
            log.error("SCM docs classpath resource load failed docId={}", safeDocId, exception);
            throw new UncheckedIOException("Could not read SCM documentation resource", exception);
        }
    }

    private Map<String, ScmDocsProperties.Document> indexDocuments(ScmDocsProperties properties) {
        Map<String, ScmDocsProperties.Document> indexedDocuments = new LinkedHashMap<>();
        for (ScmDocsProperties.Document document : properties.getDocuments()) {
            if (document == null || !StringUtils.hasText(document.getId())) {
                log.warn("SCM docs configured document ignored because id is blank");
                continue;
            }
            String safeDocId = requireSafeDocId(document.getId());
            ScmDocsProperties.Document existing = indexedDocuments.putIfAbsent(safeDocId, document);
            if (existing != null) {
                log.warn("Duplicate SCM docs configured document ignored docId={}", safeDocId);
                continue;
            }
            log.debug("SCM docs configured document indexed docId={} type={} category={} moduleCode={} serviceCode={} version={}",
                    safeDocId, document.getType(), document.getCategory(),
                    firstText(document.getModuleCode(), properties.getModuleCode()), document.getServiceCode(), document.getVersion());
        }
        return Collections.unmodifiableMap(indexedDocuments);
    }

    private ScmDocDescriptor toDescriptor(ScmDocsProperties.Document document) {
        ScmDocType type = document.getType() == null ? ScmDocType.MARKDOWN : document.getType();
        return new ScmDocDescriptor(
                document.getId(),
                firstText(document.getModuleCode(), properties.getModuleCode()),
                document.getServiceCode(),
                document.getVersion(),
                document.getCategory(),
                type,
                document.getTitle(),
                document.getDescription(),
                document.getMediaType(),
                document.getFileName(),
                firstText(document.getHref(), hrefFor(document.getId())),
                document.getOrder()
        );
    }

    private String resolveClasspathLocation(ScmDocsProperties.Document document) {
        String root = normalizePath(properties.getClasspathRoot(), "classpathRoot");
        String location = document.getClasspathLocation();
        if (!StringUtils.hasText(location)) {
            ScmDocType type = document.getType() == null ? ScmDocType.MARKDOWN : document.getType();
            location = document.getId() + type.getDefaultExtension();
        }
        location = removeClasspathPrefix(location.trim());
        rejectUnsafePathSegments(location.replace('\\', '/'), "classpathLocation");

        Path rootPath = Path.of(root).normalize();
        Path resolvedPath = rootPath.resolve(location.replace('\\', '/')).normalize();
        if (!resolvedPath.startsWith(rootPath)) {
            log.warn("SCM docs classpath path rejected because it leaves configured root docId={}", document.getId());
            throw new IllegalArgumentException("Classpath documentation location must stay under configured root");
        }

        String resolved = resolvedPath.toString().replace('\\', '/');
        rejectUnsafePathSegments(resolved, "classpathLocation");
        log.debug("SCM docs classpath href resolved docId={} href={}", document.getId(), hrefFor(document.getId()));
        return resolved;
    }

    private String normalizePath(String value, String name) {
        if (!StringUtils.hasText(value)) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        String normalized = removeClasspathPrefix(value.trim()).replace('\\', '/');
        rejectUnsafePathSegments(normalized, name);
        if (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        if (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        if (!StringUtils.hasText(normalized)) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return normalized;
    }

    private String removeClasspathPrefix(String value) {
        if (value.startsWith(CLASSPATH_PREFIX)) {
            return value.substring(CLASSPATH_PREFIX.length());
        }
        return value;
    }

    private String requireSafeDocId(String docId) {
        if (!StringUtils.hasText(docId)) {
            log.warn("SCM docs rejected blank document id");
            throw new IllegalArgumentException("Document id must not be blank");
        }
        String safeDocId = docId.trim();
        if (safeDocId.contains("/") || safeDocId.contains("\\") || safeDocId.contains("..") || safeDocId.indexOf('\0') >= 0) {
            log.warn("SCM docs rejected unsafe document id docId={}", safeDocId);
            throw new IllegalArgumentException("Document id contains unsafe path characters");
        }
        return safeDocId;
    }

    private void rejectUnsafePathSegments(String value, String name) {
        if (value.indexOf('\0') >= 0) {
            log.warn("SCM docs rejected unsafe classpath path reason=null-byte field={}", name);
            throw new IllegalArgumentException(name + " contains an unsafe null byte");
        }
        String normalized = value.replace('\\', '/');
        if (normalized.startsWith("/") || normalized.matches("^[A-Za-z]:/.*") || normalized.contains("//")) {
            log.warn("SCM docs rejected unsafe classpath path reason=absolute-or-empty-segment field={}", name);
            throw new IllegalArgumentException(name + " must be a relative classpath path");
        }
        for (String segment : normalized.split("/")) {
            if ("..".equals(segment)) {
                log.warn("SCM docs rejected unsafe classpath path reason=path-traversal field={}", name);
                throw new IllegalArgumentException(name + " must not contain path traversal segments");
            }
        }
    }

    private String hrefFor(String docId) {
        return properties.normalizedBasePath() + "/api/" + urlEncode(docId);
    }

    private String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }

    private String firstText(String first, String second) {
        if (StringUtils.hasText(first)) {
            return first.trim();
        }
        return StringUtils.hasText(second) ? second.trim() : null;
    }
}
