package ir.daneshrefah.scm.web.docs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.data.entity.asset.ChannelServiceAccessEntity;
import ir.daneshrefah.scm.common.data.entity.asset.ServiceEntity;
import ir.daneshrefah.scm.common.data.entity.definition.DefinitionEntity;
import ir.daneshrefah.scm.common.model.gateway.ChannelServiceDefinitionType;
import ir.daneshrefah.scm.core.entity.gateway.ChannelServiceDefinitionEntity;
import ir.daneshrefah.scm.core.entity.gateway.GatewayChannelEntity;
import ir.daneshrefah.scm.core.repository.gateway.ChannelServiceDefinitionRepository;
import ir.daneshrefah.scm.docs.client.model.ScmDocCategory;
import ir.daneshrefah.scm.docs.client.model.ScmDocContent;
import ir.daneshrefah.scm.docs.client.model.ScmDocDescriptor;
import ir.daneshrefah.scm.docs.client.model.ScmDocType;
import ir.daneshrefah.scm.docs.client.provider.ScmDocCatalogProvider;
import ir.daneshrefah.scm.docs.client.provider.ScmDocContentProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ScmWebRuntimeApiDocCatalogProvider implements ScmDocCatalogProvider, ScmDocContentProvider {

    private static final Logger log = LoggerFactory.getLogger(ScmWebRuntimeApiDocCatalogProvider.class);

    private static final String MODULE_CODE = "scm-web";
    private static final String SOURCE_TYPE_CLASSPATH = "CLASSPATH";
    private static final String CLASSPATH_PREFIX = "classpath:";
    private static final String SERVICES_PREFIX = "services/";
    private static final String LEGACY_DOCS_PREFIX = "scm-docs/";
    private static final String UNVERSIONED = "unversioned";

    private final ChannelServiceDefinitionRepository channelServiceDefinitionRepository;
    private final ObjectMapper objectMapper;
    private final ResourceLoader resourceLoader;

    public ScmWebRuntimeApiDocCatalogProvider(ChannelServiceDefinitionRepository channelServiceDefinitionRepository,
                                              ObjectMapper objectMapper,
                                              ResourceLoader resourceLoader) {
        this.channelServiceDefinitionRepository = channelServiceDefinitionRepository;
        this.objectMapper = objectMapper;
        this.resourceLoader = resourceLoader;
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<ScmDocDescriptor> findAll() {
        return runtimeDocumentsById().values().stream()
                .map(RuntimeApiDocument::descriptor)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ScmDocContent> findById(String docId) {
        String safeDocId = requireSafeDocId(docId);
        RuntimeApiDocument document = runtimeDocumentsById().get(safeDocId);
        if (document == null) {
            log.debug("SCM web runtime API doc not found docId={}", safeDocId);
            return Optional.empty();
        }

        Resource resource = resourceLoader.getResource(CLASSPATH_PREFIX + document.sourcePath());
        if (!resource.exists()) {
            log.warn("SCM web runtime API doc resource missing docId={} sourceType={} serviceCode={} version={}",
                    document.descriptor().id(), document.sourceType(), document.descriptor().serviceCode(),
                    document.descriptor().version());
            return Optional.empty();
        }
        if (!resource.isReadable()) {
            log.warn("SCM web runtime API doc resource unreadable docId={} sourceType={} serviceCode={} version={}",
                    document.descriptor().id(), document.sourceType(), document.descriptor().serviceCode(),
                    document.descriptor().version());
            return Optional.empty();
        }

        try (InputStream inputStream = resource.getInputStream()) {
            return Optional.of(new ScmDocContent(
                    document.descriptor(),
                    inputStream.readAllBytes(),
                    document.descriptor().mediaType(),
                    document.descriptor().fileName()
            ));
        } catch (IOException exception) {
            log.error("SCM web runtime API doc resource load failed docId={} sourceType={} serviceCode={} version={}",
                    document.descriptor().id(), document.sourceType(), document.descriptor().serviceCode(),
                    document.descriptor().version(), exception);
            throw new UncheckedIOException("Could not read SCM web runtime API documentation resource", exception);
        }
    }

    private Map<String, RuntimeApiDocument> runtimeDocumentsById() {
        List<ChannelServiceDefinitionEntity> apiDocDefinitions = channelServiceDefinitionRepository
                .findByType(ChannelServiceDefinitionType.API_DOC);
        validateSingleApiDocRowPerExposure(apiDocDefinitions);

        Map<String, RuntimeApiDocument> documentsById = new LinkedHashMap<>();
        for (ChannelServiceDefinitionEntity apiDocDefinition : apiDocDefinitions) {
            for (RuntimeApiDocument document : parseDocuments(apiDocDefinition)) {
                RuntimeApiDocument existing = documentsById.putIfAbsent(document.descriptor().id(), document);
                if (existing != null && !existing.sameContentAs(document)) {
                    throw invalidDefinition(apiDocDefinition, "duplicate runtime API doc id " + document.descriptor().id());
                }
                if (existing != null) {
                    log.debug("Duplicate SCM web runtime API doc descriptor ignored docId={} serviceCode={} version={}",
                            document.descriptor().id(), document.descriptor().serviceCode(), document.descriptor().version());
                }
            }
        }
        log.debug("SCM web runtime API doc catalog resolved apiDocDefinitions={} documents={}",
                apiDocDefinitions.size(), documentsById.size());
        return documentsById;
    }

    private void validateSingleApiDocRowPerExposure(List<ChannelServiceDefinitionEntity> apiDocDefinitions) {
        Map<String, ChannelServiceDefinitionEntity> definitionsByExposure = new LinkedHashMap<>();
        for (ChannelServiceDefinitionEntity apiDocDefinition : apiDocDefinitions) {
            String exposureKey = exposureKey(apiDocDefinition);
            ChannelServiceDefinitionEntity existing = definitionsByExposure.putIfAbsent(exposureKey, apiDocDefinition);
            if (existing != null) {
                throw invalidDefinition(apiDocDefinition,
                        "at most one API_DOC definition is allowed for each gateway/channel-service-access exposure");
            }
        }
    }

    private List<RuntimeApiDocument> parseDocuments(ChannelServiceDefinitionEntity apiDocDefinition) {
        DefinitionEntity definition = requireDefinition(apiDocDefinition);
        JsonNode details = readDetails(apiDocDefinition, definition);
        JsonNode documents = details.get("documents");
        if (documents == null || !documents.isArray()) {
            throw invalidDefinition(apiDocDefinition, "API_DOC details must contain documents[]");
        }
        if (documents.size() == 0) {
            throw invalidDefinition(apiDocDefinition, "API_DOC details documents[] must not be empty");
        }

        String version = optionalText(details, "version");
        return parseDocumentItems(apiDocDefinition, documents, version);
    }

    private List<RuntimeApiDocument> parseDocumentItems(ChannelServiceDefinitionEntity apiDocDefinition,
                                                        JsonNode documents,
                                                        String version) {
        List<RuntimeApiDocument> parsedDocuments = new ArrayList<>();
        documents.elements().forEachRemaining(document ->
                parsedDocuments.add(parseDocument(apiDocDefinition, document, version)));
        return parsedDocuments;
    }

    private RuntimeApiDocument parseDocument(ChannelServiceDefinitionEntity apiDocDefinition,
                                             JsonNode document,
                                             String version) {
        if (!document.isObject()) {
            throw invalidDefinition(apiDocDefinition, "each API_DOC documents[] item must be an object");
        }

        ScmDocType docType = docType(apiDocDefinition, requiredText(apiDocDefinition, document, "docType"));
        String name = requiredText(apiDocDefinition, document, "name");
        JsonNode source = document.get("source");
        if (source == null || !source.isObject()) {
            throw invalidDefinition(apiDocDefinition, "each API_DOC document must contain source.type and source.path");
        }
        String sourceType = requiredText(apiDocDefinition, source, "type").toUpperCase(Locale.ROOT);
        if (!SOURCE_TYPE_CLASSPATH.equals(sourceType)) {
            throw invalidDefinition(apiDocDefinition, "unsupported API_DOC source.type " + sourceType);
        }
        String sourcePath = validateSourcePath(apiDocDefinition, requiredText(apiDocDefinition, source, "path"));
        String serviceCode = serviceCode(apiDocDefinition);
        String docId = docId(serviceCode, version, docType, name);
        ScmDocDescriptor descriptor = new ScmDocDescriptor(
                docId,
                MODULE_CODE,
                serviceCode,
                version,
                ScmDocCategory.API,
                docType,
                textMap(apiDocDefinition, document.get("title"), "title"),
                textMap(apiDocDefinition, document.get("description"), "description"),
                optionalText(document, "mediaType"),
                name,
                null,
                order(document)
        );
        return new RuntimeApiDocument(descriptor, sourceType, sourcePath);
    }

    private DefinitionEntity requireDefinition(ChannelServiceDefinitionEntity apiDocDefinition) {
        DefinitionEntity definition = apiDocDefinition.getDefinition();
        if (definition == null || !StringUtils.hasText(definition.getDetails())) {
            throw invalidDefinition(apiDocDefinition, "API_DOC definition details must not be blank");
        }
        return definition;
    }

    private JsonNode readDetails(ChannelServiceDefinitionEntity apiDocDefinition, DefinitionEntity definition) {
        try {
            JsonNode details = objectMapper.readTree(definition.getDetails());
            if (details == null || !details.isObject()) {
                throw invalidDefinition(apiDocDefinition, "API_DOC definition details must be a JSON object");
            }
            return details;
        } catch (JsonProcessingException exception) {
            throw invalidDefinition(apiDocDefinition, "API_DOC definition details must be valid JSON", exception);
        }
    }

    private ScmDocType docType(ChannelServiceDefinitionEntity apiDocDefinition, String value) {
        try {
            return ScmDocType.valueOf(value.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw invalidDefinition(apiDocDefinition, "unsupported API_DOC docType " + value);
        }
    }

    private String validateSourcePath(ChannelServiceDefinitionEntity apiDocDefinition, String rawPath) {
        String sourcePath = rawPath.trim().replace('\\', '/');
        rejectUnsafePath(apiDocDefinition, sourcePath);
        if (sourcePath.startsWith(LEGACY_DOCS_PREFIX)) {
            throw invalidDefinition(apiDocDefinition, "API_DOC source.path must not start with scm-docs/");
        }
        if (!sourcePath.startsWith(SERVICES_PREFIX)) {
            throw invalidDefinition(apiDocDefinition, "API_DOC source.path must start with services/");
        }
        return sourcePath;
    }

    private void rejectUnsafePath(ChannelServiceDefinitionEntity apiDocDefinition, String sourcePath) {
        if (sourcePath.indexOf('\0') >= 0) {
            throw invalidDefinition(apiDocDefinition, "API_DOC source.path must not contain null bytes");
        }
        if (sourcePath.startsWith("/") || sourcePath.matches("^[A-Za-z]:/.*") || sourcePath.contains("//")) {
            throw invalidDefinition(apiDocDefinition, "API_DOC source.path must be a relative classpath path");
        }
        for (String segment : sourcePath.split("/")) {
            if (!StringUtils.hasText(segment) || ".".equals(segment) || "..".equals(segment)) {
                throw invalidDefinition(apiDocDefinition, "API_DOC source.path must not contain path traversal");
            }
        }
    }

    private String docId(String serviceCode, String version, ScmDocType docType, String name) {
        return MODULE_CODE
                + "."
                + normalizeSegment(serviceCode)
                + "."
                + normalizeVersion(version)
                + "."
                + docType.name()
                + "."
                + normalizeSegment(name);
    }

    private String serviceCode(ChannelServiceDefinitionEntity apiDocDefinition) {
        ChannelServiceAccessEntity access = apiDocDefinition.getChannelServiceAccess();
        ServiceEntity service = access != null ? access.getService() : null;
        if (service == null || !StringUtils.hasText(service.getCode())) {
            throw invalidDefinition(apiDocDefinition, "API_DOC service code is required");
        }
        return service.getCode().trim();
    }

    private String exposureKey(ChannelServiceDefinitionEntity apiDocDefinition) {
        GatewayChannelEntity gatewayChannel = apiDocDefinition.getGatewayChannel();
        ChannelServiceAccessEntity access = apiDocDefinition.getChannelServiceAccess();
        if (gatewayChannel == null || !StringUtils.hasText(gatewayChannel.getId())) {
            throw invalidDefinition(apiDocDefinition, "API_DOC gateway channel is required");
        }
        if (access == null || access.getId() == null) {
            throw invalidDefinition(apiDocDefinition, "API_DOC channel service access is required");
        }
        return gatewayChannel.getId() + ":" + access.getId();
    }

    private String normalizeVersion(String version) {
        return StringUtils.hasText(version) ? normalizeSegment(version) : UNVERSIONED;
    }

    private String normalizeSegment(String value) {
        String normalized = value.trim()
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "")
                .replaceAll("-+", "-");
        if (!StringUtils.hasText(normalized)) {
            throw new IllegalStateException("API_DOC document id segment must not be blank after normalization");
        }
        return normalized;
    }

    private Map<String, String> textMap(ChannelServiceDefinitionEntity apiDocDefinition, JsonNode node, String fieldName) {
        if (node == null || node.isNull()) {
            return Map.of();
        }
        if (!node.isObject()) {
            throw invalidDefinition(apiDocDefinition, "API_DOC document " + fieldName + " must be an object");
        }
        Map<String, String> values = new LinkedHashMap<>();
        node.fields().forEachRemaining(entry -> {
            if (StringUtils.hasText(entry.getKey()) && entry.getValue() != null && !entry.getValue().isNull()) {
                String value = entry.getValue().asText();
                if (StringUtils.hasText(value)) {
                    values.put(entry.getKey().trim(), value.trim());
                }
            }
        });
        return values;
    }

    private String requiredText(ChannelServiceDefinitionEntity apiDocDefinition, JsonNode node, String fieldName) {
        String value = optionalText(node, fieldName);
        if (!StringUtils.hasText(value)) {
            throw invalidDefinition(apiDocDefinition, "API_DOC document " + fieldName + " must not be blank");
        }
        return value.trim();
    }

    private String optionalText(JsonNode node, String fieldName) {
        if (node == null) {
            return null;
        }
        JsonNode value = node.get(fieldName);
        if (value == null || value.isNull()) {
            return null;
        }
        return StringUtils.hasText(value.asText()) ? value.asText().trim() : null;
    }

    private int order(JsonNode document) {
        JsonNode order = document.get("order");
        return order != null && order.canConvertToInt() ? order.asInt() : 0;
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

    private IllegalStateException invalidDefinition(ChannelServiceDefinitionEntity apiDocDefinition, String reason) {
        return invalidDefinition(apiDocDefinition, reason, null);
    }

    private IllegalStateException invalidDefinition(ChannelServiceDefinitionEntity apiDocDefinition,
                                                    String reason,
                                                    Exception cause) {
        String id = apiDocDefinition != null ? apiDocDefinition.getId() : null;
        String message = "Invalid SCM web API_DOC definition"
                + (StringUtils.hasText(id) ? " id=" + id : "")
                + ": "
                + reason;
        return cause == null ? new IllegalStateException(message) : new IllegalStateException(message, cause);
    }

    private record RuntimeApiDocument(
            ScmDocDescriptor descriptor,
            String sourceType,
            String sourcePath
    ) {
        private boolean sameContentAs(RuntimeApiDocument other) {
            return descriptor.type() == other.descriptor.type()
                    && descriptor.fileName().equals(other.descriptor.fileName())
                    && descriptor.mediaType().equals(other.descriptor.mediaType())
                    && sourceType.equals(other.sourceType)
                    && sourcePath.equals(other.sourcePath);
        }
    }
}
