package ir.daneshrefah.scm.docs.client.model;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

public record ScmDocContent(
        ScmDocDescriptor descriptor,
        byte[] body,
        String mediaType,
        String fileName
) {

    public ScmDocContent {
        Objects.requireNonNull(descriptor, "descriptor must not be null");
        body = body == null ? new byte[0] : body.clone();
        mediaType = hasText(mediaType) ? mediaType.trim() : descriptor.mediaType();
        fileName = hasText(fileName) ? fileName.trim() : descriptor.fileName();
    }

    public static ScmDocContent fromString(ScmDocDescriptor descriptor, String body) {
        byte[] bytes = body == null ? new byte[0] : body.getBytes(StandardCharsets.UTF_8);
        return new ScmDocContent(descriptor, bytes, descriptor.mediaType(), descriptor.fileName());
    }

    @Override
    public byte[] body() {
        return body.clone();
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
