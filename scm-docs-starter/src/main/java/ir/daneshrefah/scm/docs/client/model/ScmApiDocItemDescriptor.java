package ir.daneshrefah.scm.docs.client.model;

import java.util.Map;

public record ScmApiDocItemDescriptor(
        String id,
        ScmDocType docType,
        Map<String, String> title,
        Map<String, String> description,
        String mediaType,
        String fileName,
        String href,
        int order
) {

    public ScmApiDocItemDescriptor {
        id = requireText(id, "id");
        docType = docType == null ? ScmDocType.MARKDOWN : docType;
        title = ScmDocDescriptor.normalizeMap(title, Map.of("en", id));
        description = ScmDocDescriptor.normalizeMap(description, Map.of());
        mediaType = hasText(mediaType) ? mediaType.trim() : docType.getDefaultMediaType();
        fileName = hasText(fileName) ? fileName.trim() : id + docType.getDefaultExtension();
        href = trimToNull(href);
    }

    public ScmApiDocItemDescriptor withHref(String href) {
        return new ScmApiDocItemDescriptor(
                id,
                docType,
                title,
                description,
                mediaType,
                fileName,
                href,
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
