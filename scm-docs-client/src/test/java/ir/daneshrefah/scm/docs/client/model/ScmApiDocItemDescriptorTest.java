package ir.daneshrefah.scm.docs.client.model;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ScmApiDocItemDescriptorTest {

    @Test
    void withHrefSetsHref() {
        ScmApiDocItemDescriptor descriptor = descriptor(null);

        ScmApiDocItemDescriptor updated = descriptor.withHref(" /docs/api/openapi ");

        assertThat(updated.href()).isEqualTo("/docs/api/openapi");
    }

    @Test
    void descriptorDoesNotExposeDownloadUrl() {
        assertThat(Arrays.stream(ScmApiDocItemDescriptor.class.getRecordComponents())
                .map(component -> component.getName())
                .toList())
                .doesNotContain("downloadUrl");
    }

    private ScmApiDocItemDescriptor descriptor(String href) {
        return new ScmApiDocItemDescriptor(
                "scm-web.card-inquiry.v1.OPENAPI_JSON.openapi-json",
                ScmDocType.OPENAPI_JSON,
                Map.of("en", "OpenAPI"),
                Map.of(),
                "application/json",
                "openapi.json",
                href,
                10
        );
    }
}
