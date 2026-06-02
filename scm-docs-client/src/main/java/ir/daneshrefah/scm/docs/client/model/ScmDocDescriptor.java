package ir.daneshrefah.scm.docs.client.model;

import java.util.Objects;

public record ScmDocDescriptor(
        String id,
        String title,
        String description,
        ScmDocType type,
        ScmDocCategory category
) {

    public ScmDocDescriptor {
        id = requireText(id, "id");
        title = hasText(title) ? title.trim() : id;
        description = description == null ? "" : description.trim();
        type = type == null ? ScmDocType.MARKDOWN : type;
        category = category == null ? ScmDocCategory.GENERAL : category;
    }

    private static String requireText(String value, String name) {
        if (!hasText(value)) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value.trim();
    }

    private static boolean hasText(String value) {
        return Objects.nonNull(value) && !value.trim().isEmpty();
    }
}
