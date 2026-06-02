package ir.daneshrefah.scm.docs.client.model;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ScmDocTypeTest {

    @Test
    void containsOnlyPhaseOneTypes() {
        assertThat(ScmDocType.values()).containsExactly(
                ScmDocType.OPENAPI_JSON,
                ScmDocType.WSDL,
                ScmDocType.ISO8583_SCHEMA,
                ScmDocType.MARKDOWN,
                ScmDocType.HTML
        );
    }

    @Test
    void exposesDefaultMediaTypesAndExtensions() {
        assertThat(ScmDocType.OPENAPI_JSON.getDefaultMediaType()).isEqualTo("application/json");
        assertThat(ScmDocType.OPENAPI_JSON.getDefaultExtension()).isEqualTo(".json");
        assertThat(ScmDocType.WSDL.getDefaultMediaType()).isEqualTo("application/xml");
        assertThat(ScmDocType.WSDL.getDefaultExtension()).isEqualTo(".wsdl");
        assertThat(ScmDocType.ISO8583_SCHEMA.getDefaultMediaType()).isEqualTo("application/json");
        assertThat(ScmDocType.ISO8583_SCHEMA.getDefaultExtension()).isEqualTo(".json");
        assertThat(ScmDocType.MARKDOWN.getDefaultMediaType()).isEqualTo("text/markdown; charset=UTF-8");
        assertThat(ScmDocType.MARKDOWN.getDefaultExtension()).isEqualTo(".md");
        assertThat(ScmDocType.HTML.getDefaultMediaType()).isEqualTo("text/html; charset=UTF-8");
        assertThat(ScmDocType.HTML.getDefaultExtension()).isEqualTo(".html");
    }

    @Test
    void resolvesTitleWithRequestedLanguageAndFallbacks() {
        ScmDocDescriptor descriptor = new ScmDocDescriptor(
                "sample",
                "scm-web",
                null,
                null,
                ScmDocCategory.GUIDE,
                ScmDocType.MARKDOWN,
                Map.of("fa", "FA title", "en", "English title", "id", "id title"),
                Map.of("en", "English description"),
                null,
                null,
                null,
                10
        );

        assertThat(descriptor.titleFor("en-US")).isEqualTo("English title");
        assertThat(descriptor.titleFor("de")).isEqualTo("FA title");
        assertThat(descriptor.descriptionFor("fa")).isEqualTo("English description");
    }
}
