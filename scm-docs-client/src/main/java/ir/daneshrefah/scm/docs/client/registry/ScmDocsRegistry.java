package ir.daneshrefah.scm.docs.client.registry;

import ir.daneshrefah.scm.docs.client.model.ScmApiDocGroupDescriptor;
import ir.daneshrefah.scm.docs.client.model.ScmApiDocItemDescriptor;
import ir.daneshrefah.scm.docs.client.model.ScmDocContent;
import ir.daneshrefah.scm.docs.client.model.ScmDocDescriptor;
import ir.daneshrefah.scm.docs.client.provider.ScmApiDocGroupCatalogProvider;
import ir.daneshrefah.scm.docs.client.provider.ScmDocCatalogProvider;
import ir.daneshrefah.scm.docs.client.provider.ScmDocContentProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ScmDocsRegistry {

    private static final Logger log = LoggerFactory.getLogger(ScmDocsRegistry.class);

    private final List<ScmDocCatalogProvider> catalogProviders;

    private final List<ScmDocContentProvider> contentProviders;

    private final List<ScmApiDocGroupCatalogProvider> apiDocGroupCatalogProviders;

    public ScmDocsRegistry(List<ScmDocCatalogProvider> catalogProviders,
                           List<ScmDocContentProvider> contentProviders,
                           List<ScmApiDocGroupCatalogProvider> apiDocGroupCatalogProviders) {
        this.catalogProviders = List.copyOf(catalogProviders);
        this.contentProviders = List.copyOf(contentProviders);
        this.apiDocGroupCatalogProviders = List.copyOf(apiDocGroupCatalogProviders);
        log.info("SCM docs registry created catalogProviders={} contentProviders={} apiDocGroupCatalogProviders={}",
                this.catalogProviders.size(), this.contentProviders.size(), this.apiDocGroupCatalogProviders.size());
    }

    public List<ScmDocDescriptor> findAll() {
        Map<String, ScmDocDescriptor> descriptorsById = new LinkedHashMap<>();
        for (ScmDocCatalogProvider provider : catalogProviders) {
            for (ScmDocDescriptor descriptor : provider.findAll()) {
                ScmDocDescriptor existing = descriptorsById.putIfAbsent(descriptor.id(), descriptor);
                if (existing != null) {
                    log.warn("Duplicate SCM docs descriptor ignored docId={} moduleCode={} type={} category={}",
                            descriptor.id(), descriptor.moduleCode(), descriptor.type(), descriptor.category());
                }
            }
        }
        List<ScmDocDescriptor> descriptors = descriptorsById.values().stream()
                .sorted(Comparator
                        .comparingInt(ScmDocDescriptor::order)
                        .thenComparing(ScmDocDescriptor::category)
                        .thenComparing(document -> document.titleFor("en"))
                        .thenComparing(ScmDocDescriptor::id))
                .toList();
        log.debug("SCM docs catalog resolved documentCount={}", descriptors.size());
        return descriptors;
    }

    public List<ScmApiDocGroupDescriptor> findApiDocGroups() {
        Map<String, ScmApiDocGroupDescriptor> groupsById = new LinkedHashMap<>();
        for (ScmApiDocGroupCatalogProvider provider : apiDocGroupCatalogProviders) {
            for (ScmApiDocGroupDescriptor group : provider.findApiDocGroups()) {
                ScmApiDocGroupDescriptor existing = groupsById.putIfAbsent(group.id(), group);
                if (existing != null) {
                    log.warn("Duplicate SCM API doc group ignored groupId={} moduleCode={} serviceCode={} version={}",
                            group.id(), group.moduleCode(), group.serviceCode(), group.version());
                }
            }
        }
        List<ScmApiDocGroupDescriptor> groups = groupsById.values().stream()
                .map(group -> group.withDocuments(sortedItems(group)))
                .sorted(Comparator
                        .comparingInt(ScmApiDocGroupDescriptor::order)
                        .thenComparing(group -> nullSafe(group.serviceCode()))
                        .thenComparing(group -> nullSafe(group.version()))
                        .thenComparing(group -> nullSafe(group.gatewayName()))
                        .thenComparing(ScmApiDocGroupDescriptor::id))
                .toList();
        log.debug("SCM API docs catalog resolved groupCount={}", groups.size());
        return groups;
    }

    public Optional<ScmDocContent> findById(String docId) {
        log.debug("SCM docs lookup started docId={} contentProviders={}", docId, contentProviders.size());
        for (ScmDocContentProvider provider : contentProviders) {
            Optional<ScmDocContent> content = provider.findById(docId);
            if (content.isPresent()) {
                ScmDocDescriptor descriptor = content.get().descriptor();
                log.debug("SCM docs lookup matched docId={} type={} category={} moduleCode={} serviceCode={} version={}",
                        descriptor.id(), descriptor.type(), descriptor.category(), descriptor.moduleCode(),
                        descriptor.serviceCode(), descriptor.version());
                return content;
            }
        }
        log.warn("SCM docs lookup missing docId={}", docId);
        return Optional.empty();
    }

    private List<ScmApiDocItemDescriptor> sortedItems(ScmApiDocGroupDescriptor group) {
        return group.documents().stream()
                .sorted(Comparator
                        .comparingInt(ScmApiDocItemDescriptor::order)
                        .thenComparing(item -> item.docType().name())
                        .thenComparing(item -> nullSafe(item.fileName()))
                        .thenComparing(ScmApiDocItemDescriptor::id))
                .toList();
    }

    private String nullSafe(String value) {
        return value != null ? value : "";
    }
}
