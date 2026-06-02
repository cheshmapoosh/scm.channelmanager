package ir.daneshrefah.scm.docs.client.model;

import java.util.Objects;

public record ScmDocContent(
        ScmDocDescriptor descriptor,
        String content,
        String contentType
) {

    public ScmDocContent {
        Objects.requireNonNull(descriptor, "descriptor must not be null");
        content = content == null ? "" : content;
        contentType = contentType == null ? descriptor.type().getContentType() : contentType;
    }
}
