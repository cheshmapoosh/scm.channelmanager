package ir.daneshrefah.scm.common.provider.message;

import java.util.List;

public record ProviderMessageCustomizerPipeline(List<Entry> entries) {

    public ProviderMessageCustomizerPipeline {
        entries = entries == null ? List.of() : List.copyOf(entries);
    }

    public static ProviderMessageCustomizerPipeline empty() {
        return new ProviderMessageCustomizerPipeline(List.of());
    }

    public boolean isEmpty() {
        return entries.isEmpty();
    }

    public List<ProviderMessageCustomizer> customizers() {
        return entries.stream().map(Entry::customizer).toList();
    }

    public record Entry(String type, ProviderMessageCustomizer customizer) {
        public Entry {
            if (type == null || type.isBlank()) {
                throw new IllegalArgumentException("Provider customizer type is required");
            }
            if (customizer == null) {
                throw new IllegalArgumentException("Provider customizer instance is required");
            }
        }

        public int order() {
            return customizer.order();
        }
    }
}
