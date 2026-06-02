package ir.daneshrefah.scm.docs.client.web;

import ir.daneshrefah.scm.docs.client.model.ScmDocCategory;
import ir.daneshrefah.scm.docs.client.model.ScmDocDescriptor;
import ir.daneshrefah.scm.docs.client.model.ScmDocType;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ScmDocsHtmlRendererTest {

    private final ScmDocsHtmlRenderer renderer = new ScmDocsHtmlRenderer();

    @Test
    void escapesHtmlAndRendersLocalizedDocumentLinks() {
        ScmDocDescriptor descriptor = new ScmDocDescriptor(
                "api-guide",
                "scm-web",
                null,
                null,
                ScmDocCategory.API,
                ScmDocType.MARKDOWN,
                Map.of("fa", "FA <Guide>", "en", "API <Guide>"),
                Map.of("fa", "Do not render <script>alert('x')</script>"),
                null,
                "api-guide.md",
                "/docs/api/api-guide",
                10
        );

        String html = renderer.render(Map.of("en", "Docs & Support"), "fa", "/docs/", List.of(descriptor));

        assertThat(html).contains("<title>Docs &amp; Support</title>");
        assertThat(html).contains("href=\"/docs/api/api-guide\"");
        assertThat(html).contains("FA &lt;Guide&gt;");
        assertThat(html).contains("Do not render &lt;script&gt;alert(&#39;x&#39;)&lt;/script&gt;");
        assertThat(html).doesNotContain("<script>alert");
    }
}
