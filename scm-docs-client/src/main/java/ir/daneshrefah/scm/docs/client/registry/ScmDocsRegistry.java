package ir.daneshrefah.scm.docs.client.registry;

import ir.daneshrefah.scm.docs.client.model.ScmDocContent;
import ir.daneshrefah.scm.docs.client.model.ScmDocDescriptor;
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

    public ScmDocsRegistry(List<ScmDocCatalogProvider> catalogProviders, List<ScmDocContentProvider> contentProviders) {
        this.catalogProviders = List.copyOf(catalogProviders);
        this.contentProviders = List.copyOf(contentProviders);
        log.info("SCM docs registry created catalogProviders={} contentProviders={}",
                this.catalogProviders.size(), this.contentProviders.size());
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
}
