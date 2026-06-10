package ir.daneshrefah.scm.docs.client.model;

import java.util.List;
import java.util.Map;

public record ScmApiDocGroupDescriptor(
        String id,
        String moduleCode,
        String gatewayName,
        Long channelServiceAccessId,
        String serviceCode,
        String version,
        Map<String, String> title,
        Map<String, String> description,
        List<ScmApiDocItemDescriptor> documents,
        int order
) {

    public ScmApiDocGroupDescriptor {
        id = requireText(id, "id");
        moduleCode = hasText(moduleCode) ? moduleCode.trim() : "scm";
        gatewayName = trimToNull(gatewayName);
        serviceCode = trimToNull(serviceCode);
        version = trimToNull(version);
        title = ScmDocDescriptor.normalizeMap(title, Map.of("en", id));
        description = ScmDocDescriptor.normalizeMap(description, Map.of());
        documents = documents == null ? List.of() : List.copyOf(documents);
    }

    public ScmApiDocGroupDescriptor withDocuments(List<ScmApiDocItemDescriptor> documents) {
        return new ScmApiDocGroupDescriptor(
                id,
                moduleCode,
                gatewayName,
                channelServiceAccessId,
                serviceCode,
                version,
                title,
                description,
                documents,
                order
        );
    }

    private static String requireText(String value, String name) {
        if (!hasText(value)) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value.trim();
    }

    private static String trimToNull(String value) {
        return hasText(value) ? value.trim() : null;
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
