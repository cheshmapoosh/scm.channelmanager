package ir.daneshrefah.scm.docs.client.provider;

import ir.daneshrefah.scm.docs.client.autoconfigure.ScmDocsProperties;
import ir.daneshrefah.scm.docs.client.model.ScmDocContent;
import ir.daneshrefah.scm.docs.client.model.ScmDocDescriptor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class ClasspathScmDocContentProvider implements ScmDocCatalogProvider, ScmDocContentProvider {

    private static final String CLASSPATH_PREFIX = "classpath:";

    private final ScmDocsProperties properties;

    private final ResourceLoader resourceLoader;

    private final Map<String, ScmDocsProperties.Document> documentsById;

    public ClasspathScmDocContentProvider(ScmDocsProperties properties, ResourceLoader resourceLoader) {
        this.properties = properties;
        this.resourceLoader = resourceLoader;
        this.documentsById = indexDocuments(properties);
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
        ScmDocsProperties.Document document = documentsById.get(safeDocId);
        if (document == null) {
            return Optional.empty();
        }

        String resourceLocation = resolveClasspathLocation(document);
        Resource resource = resourceLoader.getResource(CLASSPATH_PREFIX + resourceLocation);
        if (!resource.exists() || !resource.isReadable()) {
            return Optional.empty();
        }

        try {
            ScmDocDescriptor descriptor = toDescriptor(document);
            return Optional.of(new ScmDocContent(
                    descriptor,
                    resource.getContentAsString(StandardCharsets.UTF_8),
                    descriptor.type().getContentType()
            ));
        } catch (IOException exception) {
            throw new UncheckedIOException("Could not read SCM documentation resource " + resourceLocation, exception);
        }
    }

    private Map<String, ScmDocsProperties.Document> indexDocuments(ScmDocsProperties properties) {
        Map<String, ScmDocsProperties.Document> indexedDocuments = new LinkedHashMap<>();
        for (ScmDocsProperties.Document document : properties.getDocuments()) {
            if (document == null || !StringUtils.hasText(document.getId())) {
                continue;
            }
            String safeDocId = requireSafeDocId(document.getId());
            indexedDocuments.putIfAbsent(safeDocId, document);
        }
        return Collections.unmodifiableMap(indexedDocuments);
    }

    private ScmDocDescriptor toDescriptor(ScmDocsProperties.Document document) {
        return new ScmDocDescriptor(
                document.getId(),
                document.getTitle(),
                document.getDescription(),
                document.getType(),
                document.getCategory()
        );
    }

    private String resolveClasspathLocation(ScmDocsProperties.Document document) {
        String root = normalizePath(properties.getClasspathRoot(), "classpathRoot");
        String location = document.getClasspathLocation();
        if (!StringUtils.hasText(location)) {
            location = document.getId() + toDescriptor(document).type().getDefaultExtension();
        }
        location = removeClasspathPrefix(location.trim());
        rejectUnsafePathSegments(location.replace('\\', '/'), "classpathLocation");

        Path rootPath = Path.of(root).normalize();
        Path resolvedPath = rootPath.resolve(location.replace('\\', '/')).normalize();
        if (!resolvedPath.startsWith(rootPath)) {
            throw new IllegalArgumentException("Classpath documentation location must stay under " + root);
        }

        String resolved = resolvedPath.toString().replace('\\', '/');
        rejectUnsafePathSegments(resolved, "classpathLocation");
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
            throw new IllegalArgumentException("Document id must not be blank");
        }
        String safeDocId = docId.trim();
        if (safeDocId.contains("/") || safeDocId.contains("\\") || safeDocId.contains("..") || safeDocId.indexOf('\0') >= 0) {
            throw new IllegalArgumentException("Document id contains unsafe path characters");
        }
        return safeDocId;
    }

    private void rejectUnsafePathSegments(String value, String name) {
        if (value.indexOf('\0') >= 0) {
            throw new IllegalArgumentException(name + " contains an unsafe null byte");
        }
        String normalized = value.replace('\\', '/');
        if (normalized.startsWith("/") || normalized.contains("//")) {
            throw new IllegalArgumentException(name + " must be a relative classpath path");
        }
        for (String segment : normalized.split("/")) {
            if ("..".equals(segment)) {
                throw new IllegalArgumentException(name + " must not contain path traversal segments");
            }
        }
    }
}
