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
import ir.daneshrefah.scm.core.integration.runtime.RuntimeMode;
import ir.daneshrefah.scm.core.integration.runtime.RuntimeTargetProperties;
import ir.daneshrefah.scm.core.integration.runtime.ScmRuntimeProperties;
import ir.daneshrefah.scm.core.repository.gateway.ChannelServiceDefinitionRepository;
import ir.daneshrefah.scm.docs.client.model.ScmApiDocGroupDescriptor;
import ir.daneshrefah.scm.docs.client.model.ScmApiDocItemDescriptor;
import ir.daneshrefah.scm.docs.client.model.ScmDocCategory;
import ir.daneshrefah.scm.docs.client.model.ScmDocContent;
import ir.daneshrefah.scm.docs.client.model.ScmDocDescriptor;
import ir.daneshrefah.scm.docs.client.model.ScmDocType;
import ir.daneshrefah.scm.docs.client.autoconfigure.ScmDocsProperties;
import ir.daneshrefah.scm.docs.client.provider.ScmApiDocGroupCatalogProvider;
import ir.daneshrefah.scm.docs.client.provider.ScmDocContentProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
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
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ScmWebRuntimeApiDocCatalogProvider implements ScmApiDocGroupCatalogProvider, ScmDocContentProvider {

    private static final Logger log = LoggerFactory.getLogger(ScmWebRuntimeApiDocCatalogProvider.class);

    private static final String MODULE_CODE = "scm-web";
    private static final String SOURCE_TYPE_CLASSPATH = "CLASSPATH";
    private static final String CLASSPATH_PREFIX = "classpath:";
    private static final String SERVICES_PREFIX = "services/";
    private static final String LEGACY_DOCS_PREFIX = "scm-docs/";
    private static final String UNVERSIONED = "unversioned";
    private static final int MAX_FAILURE_MESSAGE_LENGTH = 300;
    private static final String API_DOC_CATALOG_CACHE_NAME = "scm-web-api-doc-catalog";

    private static final String API_DOC_CATALOG_RESOLUTION_STARTED = "API_DOC_CATALOG_RESOLUTION_STARTED";
    private static final String API_DOC_CATALOG_RESOLVED = "API_DOC_CATALOG_RESOLVED";
    private static final String API_DOC_GROUP_PARSED = "API_DOC_GROUP_PARSED";
    private static final String API_DOC_GROUP_SKIPPED = "API_DOC_GROUP_SKIPPED";
    private static final String API_DOC_ITEM_PARSED = "API_DOC_ITEM_PARSED";
    private static final String API_DOC_ITEM_SKIPPED = "API_DOC_ITEM_SKIPPED";
    private static final String API_DOC_CONTENT_LOAD_STARTED = "API_DOC_CONTENT_LOAD_STARTED";
    private static final String API_DOC_CONTENT_LOADED = "API_DOC_CONTENT_LOADED";
    private static final String API_DOC_CONTENT_NOT_FOUND = "API_DOC_CONTENT_NOT_FOUND";
    private static final String API_DOC_CONTENT_LOAD_FAILED = "API_DOC_CONTENT_LOAD_FAILED";
    private static final String API_DOC_DETAILS_REF_RESOLVED = "API_DOC_DETAILS_REF_RESOLVED";
    private static final String API_DOC_DETAILS_REF_LOAD_STARTED = "API_DOC_DETAILS_REF_LOAD_STARTED";
    private static final String API_DOC_DETAILS_REF_LOADED = "API_DOC_DETAILS_REF_LOADED";
    private static final String API_DOC_DETAILS_REF_LOAD_FAILED = "API_DOC_DETAILS_REF_LOAD_FAILED";
    private static final String API_DOC_CACHE_LOOKUP_STARTED = "API_DOC_CACHE_LOOKUP_STARTED";
    private static final String API_DOC_CACHE_HIT = "API_DOC_CACHE_HIT";
    private static final String API_DOC_CACHE_MISS = "API_DOC_CACHE_MISS";
    private static final String API_DOC_CACHE_PUT = "API_DOC_CACHE_PUT";
    private static final String API_DOC_CACHE_BYPASSED = "API_DOC_CACHE_BYPASSED";
    private static final String API_DOC_CACHE_UNAVAILABLE = "API_DOC_CACHE_UNAVAILABLE";
    private static final String API_DOC_CACHE_LOAD_FAILED = "API_DOC_CACHE_LOAD_FAILED";

    private final ChannelServiceDefinitionRepository channelServiceDefinitionRepository;
    private final ObjectMapper objectMapper;
    private final ResourceLoader resourceLoader;
    private final ScmRuntimeProperties scmRuntimeProperties;
    private final ScmDocsProperties docsProperties;
    private final CacheManager cacheManager;

    public ScmWebRuntimeApiDocCatalogProvider(ChannelServiceDefinitionRepository channelServiceDefinitionRepository,
                                              ObjectMapper objectMapper,
                                              ResourceLoader resourceLoader,
                                              ScmRuntimeProperties scmRuntimeProperties,
                                              ScmDocsProperties docsProperties,
                                              ObjectProvider<CacheManager> cacheManagerProvider) {
        this.channelServiceDefinitionRepository = channelServiceDefinitionRepository;
        this.objectMapper = objectMapper;
        this.resourceLoader = resourceLoader;
        this.scmRuntimeProperties = scmRuntimeProperties;
        this.docsProperties = docsProperties;
        this.cacheManager = cacheManagerProvider.getIfAvailable();
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<ScmApiDocGroupDescriptor> findApiDocGroups() {
        return runtimeCatalog().groupsById().values().stream()
                .map(RuntimeApiDocGroup::descriptor)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ScmDocContent> findById(String docId) {
        if (!apiDocsEnabled()) {
            log.debug("SCM web runtime API doc content lookup skipped because API docs are disabled docId={}", docId);
            return Optional.empty();
        }

        String safeDocId = requireSafeDocId(docId);
        boolean failFast = failFast();
        RuntimeApiDocument document = runtimeCatalog().documentsById().get(safeDocId);
        if (document == null) {
            log.warn("event={} moduleCode={} docItemId={} failFast={} outcome=skipped reason=catalog-miss",
                    API_DOC_CONTENT_NOT_FOUND, MODULE_CODE, safeDocId, failFast);
            return Optional.empty();
        }

        log.info("event={} moduleCode={} gatewayName={} channelServiceAccessId={} serviceCode={} version={} docGroupId={} docItemId={} docType={} sourceType={} sourcePath={} failFast={} outcome=started",
                API_DOC_CONTENT_LOAD_STARTED,
                MODULE_CODE,
                document.group().descriptor().gatewayName(),
                document.group().descriptor().channelServiceAccessId(),
                document.group().descriptor().serviceCode(),
                document.group().descriptor().version(),
                document.group().descriptor().id(),
                document.contentDescriptor().id(),
                document.contentDescriptor().type(),
                document.sourceType(),
                document.sourcePath(),
                failFast);

        Resource resource = resourceLoader.getResource(CLASSPATH_PREFIX + document.sourcePath());
        if (!resource.exists() || !resource.isReadable()) {
            log.warn("event={} moduleCode={} gatewayName={} channelServiceAccessId={} serviceCode={} version={} docGroupId={} docItemId={} docType={} sourceType={} sourcePath={} failFast={} outcome=skipped reason=resource-missing-or-unreadable",
                    API_DOC_CONTENT_NOT_FOUND,
                    MODULE_CODE,
                    document.group().descriptor().gatewayName(),
                    document.group().descriptor().channelServiceAccessId(),
                    document.group().descriptor().serviceCode(),
                    document.group().descriptor().version(),
                    document.group().descriptor().id(),
                    document.contentDescriptor().id(),
                    document.contentDescriptor().type(),
                    document.sourceType(),
                    document.sourcePath(),
                    failFast);
            return Optional.empty();
        }

        try (InputStream inputStream = resource.getInputStream()) {
            ScmDocContent content = new ScmDocContent(
                    document.contentDescriptor(),
                    inputStream.readAllBytes(),
                    document.contentDescriptor().mediaType(),
                    document.contentDescriptor().fileName()
            );
            log.info("event={} moduleCode={} gatewayName={} channelServiceAccessId={} serviceCode={} version={} docGroupId={} docItemId={} docType={} sourceType={} sourcePath={} failFast={} outcome=success",
                    API_DOC_CONTENT_LOADED,
                    MODULE_CODE,
                    document.group().descriptor().gatewayName(),
                    document.group().descriptor().channelServiceAccessId(),
                    document.group().descriptor().serviceCode(),
                    document.group().descriptor().version(),
                    document.group().descriptor().id(),
                    document.contentDescriptor().id(),
                    document.contentDescriptor().type(),
                    document.sourceType(),
                    document.sourcePath(),
                    failFast);
            return Optional.of(content);
        } catch (IOException exception) {
            log.error("event={} moduleCode={} gatewayName={} channelServiceAccessId={} serviceCode={} version={} docGroupId={} docItemId={} docType={} sourceType={} sourcePath={} failFast={} outcome=failed failureType={} failureMessage={}",
                    API_DOC_CONTENT_LOAD_FAILED,
                    MODULE_CODE,
                    document.group().descriptor().gatewayName(),
                    document.group().descriptor().channelServiceAccessId(),
                    document.group().descriptor().serviceCode(),
                    document.group().descriptor().version(),
                    document.group().descriptor().id(),
                    document.contentDescriptor().id(),
                    document.contentDescriptor().type(),
                    document.sourceType(),
                    document.sourcePath(),
                    failFast,
                    exception.getClass().getSimpleName(),
                    safeFailureMessage(exception),
                    exception);
            throw new UncheckedIOException("Could not read SCM web runtime API documentation resource", exception);
        }
    }

    private RuntimeApiDocCatalog runtimeCatalog() {
        RuntimeApiDocCatalogCacheKey cacheKey = catalogCacheKey();
        Optional<Cache> cache = resolveCatalogCache(cacheKey);
        if (cache.isEmpty()) {
            logCacheEvent(API_DOC_CACHE_BYPASSED, cacheKey, null, "skipped", "cache-unavailable", null, null);
            return loadRuntimeCatalog(cacheKey);
        }
        logCacheEvent(API_DOC_CACHE_LOOKUP_STARTED, cacheKey, null, "started", null, null, null);
        Cache catalogCache = cache.get();
        Cache.ValueWrapper cachedValue = catalogCache.get(cacheKey);
        if (cachedValue != null && cachedValue.get() instanceof RuntimeApiDocCatalog catalog) {
            logCacheEvent(API_DOC_CACHE_HIT, cacheKey, catalog, "success", null, null, null);
            return catalog;
        }

        logCacheEvent(API_DOC_CACHE_MISS, cacheKey, null, "success", null, null, null);
        try {
            RuntimeApiDocCatalog catalog = loadRuntimeCatalog(cacheKey);
            catalogCache.put(cacheKey, catalog);
            logCacheEvent(API_DOC_CACHE_PUT, cacheKey, catalog, "success", null, null, null);
            return catalog;
        } catch (RuntimeException exception) {
            logCacheEvent(API_DOC_CACHE_LOAD_FAILED, cacheKey, null, "failed", "catalog-load-failed",
                    exception.getClass().getSimpleName(), safeFailureMessage(exception));
            throw exception;
        }
    }

    private RuntimeApiDocCatalog loadRuntimeCatalog(RuntimeApiDocCatalogCacheKey cacheKey) {
        boolean failFast = cacheKey.failFast();
        if (!cacheKey.apiDocsEnabled()) {
            log.debug("event={} moduleCode={} failFast={} reason=api-docs-disabled",
                    API_DOC_CATALOG_RESOLVED, MODULE_CODE, failFast);
            return RuntimeApiDocCatalog.empty();
        }

        List<String> gatewayNames = cacheKey.gatewayNames();
        log.info("event={} moduleCode={} runtimeMode={} gatewayNames={} failFast={} outcome=started",
                API_DOC_CATALOG_RESOLUTION_STARTED,
                MODULE_CODE,
                cacheKey.runtimeMode(),
                gatewayNames,
                failFast);
        if (gatewayNames.isEmpty()) {
            log.info("event={} moduleCode={} runtimeMode={} gatewayNames={} failFast={} groupCount=0 itemCount=0 outcome=success",
                    API_DOC_CATALOG_RESOLVED,
                    MODULE_CODE,
                    cacheKey.runtimeMode(),
                    gatewayNames,
                    failFast);
            return RuntimeApiDocCatalog.empty();
        }

        List<ChannelServiceDefinitionEntity> apiDocDefinitions = channelServiceDefinitionRepository
                .findByTypeAndGatewayChannel_NameIn(ChannelServiceDefinitionType.API_DOC, gatewayNames);
        RuntimeApiDocCatalog catalog = parseCatalog(apiDocDefinitions, failFast);
        log.info("event={} moduleCode={} runtimeMode={} gatewayNames={} failFast={} apiDocDefinitions={} groupCount={} itemCount={} outcome=success",
                API_DOC_CATALOG_RESOLVED,
                MODULE_CODE,
                cacheKey.runtimeMode(),
                gatewayNames,
                failFast,
                apiDocDefinitions.size(),
                catalog.groupsById().size(),
                catalog.documentsById().size());
        return catalog;
    }

    private RuntimeApiDocCatalogCacheKey catalogCacheKey() {
        return new RuntimeApiDocCatalogCacheKey(
                scmRuntimeProperties.runtimeMode(),
                activeRuntimeGatewayNames(),
                failFast(),
                apiDocsEnabled());
    }

    private RuntimeApiDocCatalog parseCatalog(List<ChannelServiceDefinitionEntity> apiDocDefinitions, boolean failFast) {
        Map<String, ChannelServiceDefinitionEntity> definitionsByExposure = new LinkedHashMap<>();
        Map<String, RuntimeApiDocGroup> groupsById = new LinkedHashMap<>();
        Map<String, RuntimeApiDocument> documentsById = new LinkedHashMap<>();

        for (ChannelServiceDefinitionEntity apiDocDefinition : apiDocDefinitions) {
            if (!registerExposure(definitionsByExposure, apiDocDefinition, failFast)) {
                continue;
            }

            RuntimeApiDocGroup group = parseGroupSafely(apiDocDefinition, failFast);
            if (group == null) {
                continue;
            }
            RuntimeApiDocGroup existingGroup = groupsById.putIfAbsent(group.descriptor().id(), group);
            if (existingGroup != null) {
                IllegalStateException exception = invalidDefinition(apiDocDefinition,
                        "duplicate runtime API doc group id " + group.descriptor().id());
                if (failFast) {
                    throw exception;
                }
                logGroupSkipped(apiDocDefinition, failFast, "duplicate-doc-group-id", exception);
                continue;
            }

            for (RuntimeApiDocument document : group.documents()) {
                RuntimeApiDocument existing = documentsById.putIfAbsent(document.contentDescriptor().id(), document);
                if (existing != null) {
                    IllegalStateException exception = invalidDefinition(apiDocDefinition,
                            "duplicate runtime API doc item id " + document.contentDescriptor().id());
                    if (failFast) {
                        throw exception;
                    }
                    logItemSkipped(group, document.itemDescriptor(), document.sourceType(), document.sourcePath(),
                            failFast, "duplicate-doc-item-id", exception);
                }
            }
        }

        return new RuntimeApiDocCatalog(sortedGroups(groupsById), documentsById);
    }

    private boolean registerExposure(Map<String, ChannelServiceDefinitionEntity> definitionsByExposure,
                                     ChannelServiceDefinitionEntity apiDocDefinition,
                                     boolean failFast) {
        try {
            String exposureKey = exposureKey(apiDocDefinition);
            ChannelServiceDefinitionEntity existing = definitionsByExposure.putIfAbsent(exposureKey, apiDocDefinition);
            if (existing == null) {
                return true;
            }
            throw invalidDefinition(apiDocDefinition,
                    "at most one API_DOC definition is allowed for each gateway/channel-service-access exposure");
        } catch (IllegalStateException exception) {
            if (failFast) {
                throw exception;
            }
            logGroupSkipped(apiDocDefinition, failFast, "invalid-exposure", exception);
            return false;
        }
    }

    private RuntimeApiDocGroup parseGroupSafely(ChannelServiceDefinitionEntity apiDocDefinition, boolean failFast) {
        try {
            return parseGroup(apiDocDefinition, failFast);
        } catch (IllegalStateException exception) {
            if (failFast) {
                throw exception;
            }
            logGroupSkipped(apiDocDefinition, failFast, "invalid-api-doc-group", exception);
            return null;
        }
    }

    private RuntimeApiDocGroup parseGroup(ChannelServiceDefinitionEntity apiDocDefinition, boolean failFast) {
        DefinitionEntity definition = requireDefinition(apiDocDefinition);
        JsonNode details = readDetails(apiDocDefinition, definition, failFast);
        JsonNode documents = details.get("documents");
        if (documents == null || !documents.isArray()) {
            throw invalidDefinition(apiDocDefinition, "API_DOC details must contain documents[]");
        }
        if (documents.isEmpty()) {
            throw invalidDefinition(apiDocDefinition, "API_DOC details documents[] must not be empty");
        }

        String version = optionalText(details, "version");
        String serviceCode = serviceCode(apiDocDefinition);
        String gatewayName = gatewayName(apiDocDefinition);
        Long channelServiceAccessId = channelServiceAccessId(apiDocDefinition);
        String groupId = groupId(gatewayName, channelServiceAccessId, serviceCode, version);
        List<RuntimeApiDocument> parsedDocuments = parseDocumentItems(
                apiDocDefinition,
                documents,
                groupId,
                serviceCode,
                version,
                failFast);
        if (parsedDocuments.isEmpty()) {
            throw invalidDefinition(apiDocDefinition, "API_DOC details documents[] produced no valid document items");
        }

        ScmApiDocGroupDescriptor descriptor = new ScmApiDocGroupDescriptor(
                groupId,
                MODULE_CODE,
                gatewayName,
                channelServiceAccessId,
                serviceCode,
                version,
                textMap(apiDocDefinition, details.get("title"), "title"),
                textMap(apiDocDefinition, details.get("description"), "description"),
                sortedItemDescriptors(parsedDocuments),
                order(details)
        );
        RuntimeApiDocGroup groupRef = new RuntimeApiDocGroup(descriptor, List.of());
        RuntimeApiDocGroup group = new RuntimeApiDocGroup(
                descriptor,
                parsedDocuments.stream()
                        .map(document -> document.withGroup(groupRef))
                        .toList());
        log.info("event={} moduleCode={} gatewayName={} channelServiceAccessId={} serviceCode={} version={} definitionId={} docGroupId={} failFast={} documentCount={} outcome=success",
                API_DOC_GROUP_PARSED,
                MODULE_CODE,
                gatewayName,
                channelServiceAccessId,
                serviceCode,
                version,
                safeDefinitionId(apiDocDefinition),
                groupId,
                failFast,
                parsedDocuments.size());
        return group;
    }

    private List<RuntimeApiDocument> parseDocumentItems(ChannelServiceDefinitionEntity apiDocDefinition,
                                                        JsonNode documents,
                                                        String groupId,
                                                        String serviceCode,
                                                        String version,
                                                        boolean failFast) {
        List<RuntimeApiDocument> parsedDocuments = new ArrayList<>();
        Map<String, RuntimeApiDocument> documentsById = new LinkedHashMap<>();
        documents.elements().forEachRemaining(document -> {
            RuntimeApiDocument parsed = parseDocumentSafely(
                    apiDocDefinition,
                    document,
                    groupId,
                    serviceCode,
                    version,
                    failFast);
            if (parsed == null) {
                return;
            }
            RuntimeApiDocument existing = documentsById.putIfAbsent(parsed.contentDescriptor().id(), parsed);
            if (existing != null) {
                IllegalStateException exception = invalidDefinition(apiDocDefinition,
                        "duplicate runtime API doc item id " + parsed.contentDescriptor().id());
                if (failFast) {
                    throw exception;
                }
                logItemSkipped(apiDocDefinition, groupId, version, parsed.itemDescriptor(), parsed.sourceType(), parsed.sourcePath(),
                        failFast, "duplicate-doc-item-id", exception);
                return;
            }
            parsedDocuments.add(parsed);
        });
        return parsedDocuments;
    }

    private RuntimeApiDocument parseDocumentSafely(ChannelServiceDefinitionEntity apiDocDefinition,
                                                   JsonNode document,
                                                   String groupId,
                                                   String serviceCode,
                                                   String version,
                                                   boolean failFast) {
        try {
            return parseDocument(apiDocDefinition, document, groupId, serviceCode, version, failFast);
        } catch (IllegalStateException exception) {
            if (failFast) {
                throw exception;
            }
            logItemSkipped(apiDocDefinition, groupId, version, null, null, null,
                    failFast, "invalid-api-doc-item", exception);
            return null;
        }
    }

    private RuntimeApiDocument parseDocument(ChannelServiceDefinitionEntity apiDocDefinition,
                                             JsonNode document,
                                             String groupId,
                                             String serviceCode,
                                             String version,
                                             boolean failFast) {
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
        String itemId = itemId(groupId, docType, name);
        ScmApiDocItemDescriptor itemDescriptor = new ScmApiDocItemDescriptor(
                itemId,
                docType,
                textMap(apiDocDefinition, document.get("title"), "title"),
                textMap(apiDocDefinition, document.get("description"), "description"),
                optionalText(document, "mediaType"),
                name,
                null,
                order(document)
        );
        ScmDocDescriptor contentDescriptor = new ScmDocDescriptor(
                itemId,
                MODULE_CODE,
                serviceCode,
                version,
                ScmDocCategory.API,
                docType,
                itemDescriptor.title(),
                itemDescriptor.description(),
                itemDescriptor.mediaType(),
                itemDescriptor.fileName(),
                null,
                itemDescriptor.order()
        );
        RuntimeApiDocument apiDocument = new RuntimeApiDocument(null, contentDescriptor, itemDescriptor, sourceType, sourcePath);
        log.info("event={} moduleCode={} gatewayName={} channelServiceAccessId={} serviceCode={} version={} definitionId={} docGroupId={} docItemId={} docType={} sourceType={} sourcePath={} failFast={} outcome=success",
                API_DOC_ITEM_PARSED,
                MODULE_CODE,
                safeGatewayName(apiDocDefinition),
                safeChannelServiceAccessId(apiDocDefinition),
                serviceCode,
                version,
                safeDefinitionId(apiDocDefinition),
                groupId,
                itemId,
                docType,
                sourceType,
                sourcePath,
                failFast);
        return apiDocument;
    }

    private List<ScmApiDocItemDescriptor> sortedItemDescriptors(List<RuntimeApiDocument> documents) {
        return documents.stream()
                .map(RuntimeApiDocument::itemDescriptor)
                .sorted(Comparator
                        .comparingInt(ScmApiDocItemDescriptor::order)
                        .thenComparing(item -> item.docType().name())
                        .thenComparing(item -> nullSafe(item.fileName()))
                        .thenComparing(ScmApiDocItemDescriptor::id))
                .toList();
    }

    private Map<String, RuntimeApiDocGroup> sortedGroups(Map<String, RuntimeApiDocGroup> groupsById) {
        Map<String, RuntimeApiDocGroup> sorted = new LinkedHashMap<>();
        Comparator<RuntimeApiDocGroup> groupComparator = Comparator
                .comparingInt((RuntimeApiDocGroup group) -> group.descriptor().order())
                .thenComparing(group -> nullSafe(group.descriptor().serviceCode()))
                .thenComparing(group -> nullSafe(group.descriptor().version()))
                .thenComparing(group -> nullSafe(group.descriptor().gatewayName()))
                .thenComparing(group -> group.descriptor().id());
        groupsById.values().stream()
                .sorted(groupComparator)
                .forEach(group -> sorted.put(group.descriptor().id(), group));
        return sorted;
    }

    private List<String> activeRuntimeGatewayNames() {
        RuntimeMode runtimeMode = scmRuntimeProperties.runtimeMode();
        return scmRuntimeProperties.runtimeTargets()
                .stream()
                .filter(RuntimeTargetProperties::enabled)
                .filter(runtimeTarget -> runtimeMode.accepts(runtimeTarget.targetKind()))
                .flatMap(runtimeTarget -> runtimeTarget.gatewayNames().stream())
                .distinct()
                .toList();
    }

    private DefinitionEntity requireDefinition(ChannelServiceDefinitionEntity apiDocDefinition) {
        DefinitionEntity definition = apiDocDefinition.getDefinition();
        if (definition == null || !StringUtils.hasText(definition.getDetails())) {
            throw invalidDefinition(apiDocDefinition, "API_DOC definition details must not be blank");
        }
        return definition;
    }

    private JsonNode readDetails(ChannelServiceDefinitionEntity apiDocDefinition,
                                 DefinitionEntity definition,
                                 boolean failFast) {
        try {
            JsonNode details = objectMapper.readTree(definition.getDetails());
            if (details == null || !details.isObject()) {
                throw invalidDefinition(apiDocDefinition, "API_DOC definition details must be a JSON object");
            }
            JsonNode detailsRef = details.get("detailsRef");
            if (detailsRef != null && !detailsRef.isNull()) {
                validateDetailsRefHasNoInlineFields(apiDocDefinition, details);
                return readDetailsRef(apiDocDefinition, definition, details, detailsRef, failFast);
            }
            return details;
        } catch (JsonProcessingException exception) {
            throw invalidDefinition(apiDocDefinition, "API_DOC definition details must be valid JSON", exception);
        }
    }

    private JsonNode readDetailsRef(ChannelServiceDefinitionEntity apiDocDefinition,
                                    DefinitionEntity definition,
                                    JsonNode inlineDetails,
                                    JsonNode detailsRef,
                                    boolean failFast) {
        String sourceType = null;
        String sourcePath = null;
        String version = optionalText(inlineDetails, "version");
        try {
            if (!detailsRef.isObject()) {
                throw invalidDefinition(apiDocDefinition, "API_DOC detailsRef must be a JSON object");
            }
            sourceType = requiredDetailsRefText(apiDocDefinition, detailsRef, "type").toUpperCase(Locale.ROOT);
            String rawSourcePath = requiredDetailsRefText(apiDocDefinition, detailsRef, "path");
            sourcePath = rawSourcePath.trim().replace('\\', '/');
            sourcePath = validateSourcePath(apiDocDefinition, rawSourcePath, "detailsRef.path");
            if (!SOURCE_TYPE_CLASSPATH.equals(sourceType)) {
                throw invalidDefinition(apiDocDefinition, "unsupported API_DOC detailsRef.type " + sourceType);
            }
            logDetailsRefEvent(API_DOC_DETAILS_REF_RESOLVED, apiDocDefinition, version, definition,
                    sourceType, sourcePath, failFast, "success", null, null);
            logDetailsRefEvent(API_DOC_DETAILS_REF_LOAD_STARTED, apiDocDefinition, version, definition,
                    sourceType, sourcePath, failFast, "started", null, null);

            Resource resource = resourceLoader.getResource(CLASSPATH_PREFIX + sourcePath);
            if (resource == null || !resource.exists() || !resource.isReadable()) {
                throw invalidDefinition(apiDocDefinition, "API_DOC detailsRef resource is missing or unreadable");
            }
            try (InputStream inputStream = resource.getInputStream()) {
                JsonNode referencedDetails = objectMapper.readTree(inputStream);
                if (referencedDetails == null || !referencedDetails.isObject()) {
                    throw invalidDefinition(apiDocDefinition, "API_DOC detailsRef resource must contain a JSON object");
                }
                logDetailsRefEvent(API_DOC_DETAILS_REF_LOADED, apiDocDefinition, optionalText(referencedDetails, "version"),
                        definition, sourceType, sourcePath, failFast, "success", null, null);
                return referencedDetails;
            } catch (JsonProcessingException exception) {
                throw invalidDefinition(apiDocDefinition, "API_DOC detailsRef resource must be valid JSON", exception);
            } catch (IOException exception) {
                throw invalidDefinition(apiDocDefinition, "Could not read API_DOC detailsRef resource", exception);
            }
        } catch (IllegalStateException exception) {
            logDetailsRefEvent(API_DOC_DETAILS_REF_LOAD_FAILED, apiDocDefinition, version, definition,
                    sourceType, sourcePath, failFast, "failed", "details-ref-load-failed", exception);
            throw exception;
        }
    }

    private void validateDetailsRefHasNoInlineFields(ChannelServiceDefinitionEntity apiDocDefinition, JsonNode details) {
        List<String> inlineFields = List.of("version", "title", "description", "order", "documents");
        boolean hasInlineField = inlineFields.stream()
                .anyMatch(fieldName -> details.has(fieldName) && !details.get(fieldName).isNull());
        if (hasInlineField) {
            throw invalidDefinition(apiDocDefinition,
                    "API_DOC detailsRef must not be combined with inline API doc fields: version, title, description, order, documents");
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
        return validateSourcePath(apiDocDefinition, rawPath, "source.path");
    }

    private String validateSourcePath(ChannelServiceDefinitionEntity apiDocDefinition, String rawPath, String fieldName) {
        String sourcePath = rawPath.trim().replace('\\', '/');
        rejectUnsafePath(apiDocDefinition, sourcePath, fieldName);
        if (sourcePath.startsWith(LEGACY_DOCS_PREFIX)) {
            throw invalidDefinition(apiDocDefinition, "API_DOC " + fieldName + " must not start with scm-docs/");
        }
        if (!sourcePath.startsWith(SERVICES_PREFIX)) {
            throw invalidDefinition(apiDocDefinition, "API_DOC " + fieldName + " must start with services/");
        }
        return sourcePath;
    }

    private void rejectUnsafePath(ChannelServiceDefinitionEntity apiDocDefinition, String sourcePath, String fieldName) {
        if (sourcePath.indexOf('\0') >= 0) {
            throw invalidDefinition(apiDocDefinition, "API_DOC " + fieldName + " must not contain null bytes");
        }
        if (sourcePath.startsWith("/") || sourcePath.matches("^[A-Za-z]:/.*") || sourcePath.contains("//")) {
            throw invalidDefinition(apiDocDefinition, "API_DOC " + fieldName + " must be a relative classpath path");
        }
        for (String segment : sourcePath.split("/")) {
            if (!StringUtils.hasText(segment) || ".".equals(segment) || "..".equals(segment)) {
                throw invalidDefinition(apiDocDefinition, "API_DOC " + fieldName + " must not contain path traversal");
            }
        }
    }

    private String groupId(String gatewayName, Long channelServiceAccessId, String serviceCode, String version) {
        return MODULE_CODE
                + "."
                + normalizeSegment(gatewayName)
                + "."
                + channelServiceAccessId
                + "."
                + normalizeSegment(serviceCode)
                + "."
                + normalizeVersion(version);
    }

    private String itemId(String groupId, ScmDocType docType, String name) {
        return groupId
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

    private String gatewayName(ChannelServiceDefinitionEntity apiDocDefinition) {
        GatewayChannelEntity gatewayChannel = apiDocDefinition.getGatewayChannel();
        if (gatewayChannel == null || !StringUtils.hasText(gatewayChannel.getName())) {
            throw invalidDefinition(apiDocDefinition, "API_DOC gateway name is required");
        }
        return gatewayChannel.getName().trim();
    }

    private Long channelServiceAccessId(ChannelServiceDefinitionEntity apiDocDefinition) {
        ChannelServiceAccessEntity access = apiDocDefinition.getChannelServiceAccess();
        if (access == null || access.getId() == null) {
            throw invalidDefinition(apiDocDefinition, "API_DOC channel service access is required");
        }
        return access.getId();
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

    private String requiredDetailsRefText(ChannelServiceDefinitionEntity apiDocDefinition,
                                          JsonNode node,
                                          String fieldName) {
        String value = optionalText(node, fieldName);
        if (!StringUtils.hasText(value)) {
            throw invalidDefinition(apiDocDefinition, "API_DOC detailsRef." + fieldName + " must not be blank");
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

    private boolean apiDocsEnabled() {
        return docsProperties.getApi().isEnabled();
    }

    private boolean failFast() {
        return docsProperties.getApi().isFailFast();
    }

    private Optional<Cache> resolveCatalogCache(RuntimeApiDocCatalogCacheKey cacheKey) {
        if (cacheManager == null) {
            logCacheEvent(API_DOC_CACHE_UNAVAILABLE, cacheKey, null, "skipped",
                    "cache-manager-missing", null, null);
            return Optional.empty();
        }
        if (!cacheManager.getCacheNames().contains(API_DOC_CATALOG_CACHE_NAME)) {
            logCacheEvent(API_DOC_CACHE_UNAVAILABLE, cacheKey, null, "skipped",
                    "cache-not-found", null, null);
            return Optional.empty();
        }
        Cache cache = cacheManager.getCache(API_DOC_CATALOG_CACHE_NAME);
        if (cache == null) {
            logCacheEvent(API_DOC_CACHE_UNAVAILABLE, cacheKey, null, "skipped",
                    "cache-not-found", null, null);
            return Optional.empty();
        }
        return Optional.of(cache);
    }

    private void logCacheEvent(String event,
                               RuntimeApiDocCatalogCacheKey cacheKey,
                               RuntimeApiDocCatalog catalog,
                               String outcome,
                               String reason,
                               String failureType,
                               String failureMessage) {
        int groupCount = catalog != null ? catalog.groupsById().size() : 0;
        int itemCount = catalog != null ? catalog.documentsById().size() : 0;
        if (API_DOC_CACHE_LOAD_FAILED.equals(event) || API_DOC_CACHE_UNAVAILABLE.equals(event)) {
            log.warn("event={} moduleCode={} cacheName={} runtimeMode={} gatewayNames={} failFast={} apiDocsEnabled={} cacheKey={} groupCount={} itemCount={} outcome={} reason={} failureType={} failureMessage={}",
                    event,
                    MODULE_CODE,
                    API_DOC_CATALOG_CACHE_NAME,
                    cacheKey.runtimeMode(),
                    cacheKey.gatewayNames(),
                    cacheKey.failFast(),
                    cacheKey.apiDocsEnabled(),
                    cacheKey,
                    groupCount,
                    itemCount,
                    outcome,
                    reason,
                    failureType,
                    failureMessage);
            return;
        }
        log.info("event={} moduleCode={} cacheName={} runtimeMode={} gatewayNames={} failFast={} apiDocsEnabled={} cacheKey={} groupCount={} itemCount={} outcome={} reason={} failureType={} failureMessage={}",
                event,
                MODULE_CODE,
                API_DOC_CATALOG_CACHE_NAME,
                cacheKey.runtimeMode(),
                cacheKey.gatewayNames(),
                cacheKey.failFast(),
                cacheKey.apiDocsEnabled(),
                cacheKey,
                groupCount,
                itemCount,
                outcome,
                reason,
                failureType,
                failureMessage);
    }

    private void logDetailsRefEvent(String event,
                                    ChannelServiceDefinitionEntity apiDocDefinition,
                                    String version,
                                    DefinitionEntity definition,
                                    String sourceType,
                                    String sourcePath,
                                    boolean failFast,
                                    String outcome,
                                    String reason,
                                    Exception exception) {
        String failureType = exception != null ? exception.getClass().getSimpleName() : null;
        String failureMessage = exception != null ? safeFailureMessage(exception) : null;
        String definitionId = definition != null ? definition.getId() : null;
        if (API_DOC_DETAILS_REF_LOAD_FAILED.equals(event)) {
            log.warn("event={} moduleCode={} gatewayName={} channelServiceAccessId={} serviceCode={} version={} definitionId={} sourceType={} sourcePath={} failFast={} outcome={} reason={} failureType={} failureMessage={}",
                    event,
                    MODULE_CODE,
                    safeGatewayName(apiDocDefinition),
                    safeChannelServiceAccessId(apiDocDefinition),
                    safeServiceCode(apiDocDefinition),
                    version,
                    definitionId,
                    sourceType,
                    sourcePath,
                    failFast,
                    outcome,
                    reason,
                    failureType,
                    failureMessage);
            return;
        }
        log.info("event={} moduleCode={} gatewayName={} channelServiceAccessId={} serviceCode={} version={} definitionId={} sourceType={} sourcePath={} failFast={} outcome={} reason={} failureType={} failureMessage={}",
                event,
                MODULE_CODE,
                safeGatewayName(apiDocDefinition),
                safeChannelServiceAccessId(apiDocDefinition),
                safeServiceCode(apiDocDefinition),
                version,
                definitionId,
                sourceType,
                sourcePath,
                failFast,
                outcome,
                reason,
                failureType,
                failureMessage);
    }

    private void logGroupSkipped(ChannelServiceDefinitionEntity apiDocDefinition,
                                 boolean failFast,
                                 String reason,
                                 Exception exception) {
        log.warn("event={} moduleCode={} gatewayName={} channelServiceAccessId={} serviceCode={} definitionId={} failFast={} outcome=skipped reason={} failureType={} failureMessage={}",
                API_DOC_GROUP_SKIPPED,
                MODULE_CODE,
                safeGatewayName(apiDocDefinition),
                safeChannelServiceAccessId(apiDocDefinition),
                safeServiceCode(apiDocDefinition),
                safeDefinitionId(apiDocDefinition),
                failFast,
                reason,
                exception.getClass().getSimpleName(),
                safeFailureMessage(exception));
    }

    private void logItemSkipped(RuntimeApiDocGroup group,
                                ScmApiDocItemDescriptor itemDescriptor,
                                String sourceType,
                                String sourcePath,
                                boolean failFast,
                                String reason,
                                Exception exception) {
        ScmApiDocGroupDescriptor groupDescriptor = group != null ? group.descriptor() : null;
        log.warn("event={} moduleCode={} gatewayName={} channelServiceAccessId={} serviceCode={} version={} docGroupId={} docItemId={} docType={} sourceType={} sourcePath={} failFast={} outcome=skipped reason={} failureType={} failureMessage={}",
                API_DOC_ITEM_SKIPPED,
                MODULE_CODE,
                groupDescriptor != null ? groupDescriptor.gatewayName() : null,
                groupDescriptor != null ? groupDescriptor.channelServiceAccessId() : null,
                groupDescriptor != null ? groupDescriptor.serviceCode() : null,
                groupDescriptor != null ? groupDescriptor.version() : null,
                groupDescriptor != null ? groupDescriptor.id() : null,
                itemDescriptor != null ? itemDescriptor.id() : null,
                itemDescriptor != null ? itemDescriptor.docType() : null,
                sourceType,
                sourcePath,
                failFast,
                reason,
                exception.getClass().getSimpleName(),
                safeFailureMessage(exception));
    }

    private void logItemSkipped(ChannelServiceDefinitionEntity apiDocDefinition,
                                String groupId,
                                String version,
                                ScmApiDocItemDescriptor itemDescriptor,
                                String sourceType,
                                String sourcePath,
                                boolean failFast,
                                String reason,
                                Exception exception) {
        log.warn("event={} moduleCode={} gatewayName={} channelServiceAccessId={} serviceCode={} version={} definitionId={} docGroupId={} docItemId={} docType={} sourceType={} sourcePath={} failFast={} outcome=skipped reason={} failureType={} failureMessage={}",
                API_DOC_ITEM_SKIPPED,
                MODULE_CODE,
                safeGatewayName(apiDocDefinition),
                safeChannelServiceAccessId(apiDocDefinition),
                safeServiceCode(apiDocDefinition),
                version,
                safeDefinitionId(apiDocDefinition),
                groupId,
                itemDescriptor != null ? itemDescriptor.id() : null,
                itemDescriptor != null ? itemDescriptor.docType() : null,
                sourceType,
                sourcePath,
                failFast,
                reason,
                exception.getClass().getSimpleName(),
                safeFailureMessage(exception));
    }

    private String safeGatewayName(ChannelServiceDefinitionEntity apiDocDefinition) {
        GatewayChannelEntity gatewayChannel = apiDocDefinition != null ? apiDocDefinition.getGatewayChannel() : null;
        return gatewayChannel != null ? gatewayChannel.getName() : null;
    }

    private Long safeChannelServiceAccessId(ChannelServiceDefinitionEntity apiDocDefinition) {
        ChannelServiceAccessEntity access = apiDocDefinition != null ? apiDocDefinition.getChannelServiceAccess() : null;
        return access != null ? access.getId() : null;
    }

    private String safeServiceCode(ChannelServiceDefinitionEntity apiDocDefinition) {
        ChannelServiceAccessEntity access = apiDocDefinition != null ? apiDocDefinition.getChannelServiceAccess() : null;
        ServiceEntity service = access != null ? access.getService() : null;
        return service != null ? service.getCode() : null;
    }

    private String safeDefinitionId(ChannelServiceDefinitionEntity apiDocDefinition) {
        DefinitionEntity definition = apiDocDefinition != null ? apiDocDefinition.getDefinition() : null;
        return definition != null ? definition.getId() : null;
    }

    private String safeFailureMessage(Exception exception) {
        if (exception.getMessage() == null) {
            return null;
        }
        String message = exception.getMessage()
                .replace('\r', ' ')
                .replace('\n', ' ')
                .replaceAll("(?i)(password|token|authorization|client_secret|authorization_code|pin|otp|session[_-]?id|card[_-]?number)\\s*[:=]\\s*\\S+", "$1=***")
                .trim();
        if (message.length() > MAX_FAILURE_MESSAGE_LENGTH) {
            return message.substring(0, MAX_FAILURE_MESSAGE_LENGTH);
        }
        return message;
    }

    private String nullSafe(String value) {
        return value != null ? value : "";
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

    private record RuntimeApiDocCatalog(
            Map<String, RuntimeApiDocGroup> groupsById,
            Map<String, RuntimeApiDocument> documentsById
    ) {
        private static RuntimeApiDocCatalog empty() {
            return new RuntimeApiDocCatalog(Map.of(), Map.of());
        }
    }

    private record RuntimeApiDocCatalogCacheKey(
            RuntimeMode runtimeMode,
            List<String> gatewayNames,
            boolean failFast,
            boolean apiDocsEnabled
    ) {
        private RuntimeApiDocCatalogCacheKey {
            gatewayNames = gatewayNames == null ? List.of() : List.copyOf(gatewayNames);
        }
    }

    private record RuntimeApiDocGroup(
            ScmApiDocGroupDescriptor descriptor,
            List<RuntimeApiDocument> documents
    ) {
        private RuntimeApiDocGroup {
            documents = documents == null ? List.of() : List.copyOf(documents);
        }
    }

    private record RuntimeApiDocument(
            RuntimeApiDocGroup group,
            ScmDocDescriptor contentDescriptor,
            ScmApiDocItemDescriptor itemDescriptor,
            String sourceType,
            String sourcePath
    ) {
        private RuntimeApiDocument withGroup(RuntimeApiDocGroup group) {
            return new RuntimeApiDocument(group, contentDescriptor, itemDescriptor, sourceType, sourcePath);
        }
    }
}
