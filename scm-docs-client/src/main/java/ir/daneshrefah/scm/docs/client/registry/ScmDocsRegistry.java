package ir.daneshrefah.scm.docs.client.registry;

import ir.daneshrefah.scm.docs.client.model.ScmDocContent;
import ir.daneshrefah.scm.docs.client.model.ScmDocDescriptor;
import ir.daneshrefah.scm.docs.client.provider.ScmDocCatalogProvider;
import ir.daneshrefah.scm.docs.client.provider.ScmDocContentProvider;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ScmDocsRegistry {

    private final List<ScmDocCatalogProvider> catalogProviders;

    private final List<ScmDocContentProvider> contentProviders;

    public ScmDocsRegistry(List<ScmDocCatalogProvider> catalogProviders, List<ScmDocContentProvider> contentProviders) {
        this.catalogProviders = List.copyOf(catalogProviders);
        this.contentProviders = List.copyOf(contentProviders);
    }

    public List<ScmDocDescriptor> findAll() {
        Map<String, ScmDocDescriptor> descriptorsById = new LinkedHashMap<>();
        for (ScmDocCatalogProvider provider : catalogProviders) {
            for (ScmDocDescriptor descriptor : provider.findAll()) {
                descriptorsById.putIfAbsent(descriptor.id(), descriptor);
            }
        }
        return descriptorsById.values().stream()
                .sorted(Comparator
                        .comparing(ScmDocDescriptor::category)
                        .thenComparing(ScmDocDescriptor::title)
                        .thenComparing(ScmDocDescriptor::id))
                .toList();
    }

    public Optional<ScmDocContent> findById(String docId) {
        for (ScmDocContentProvider provider : contentProviders) {
            Optional<ScmDocContent> content = provider.findById(docId);
            if (content.isPresent()) {
                return content;
            }
        }
        return Optional.empty();
    }
}
