package ir.daneshrefah.scm.docs.client.web;

import ir.daneshrefah.scm.docs.client.model.ScmDocCategory;
import ir.daneshrefah.scm.docs.client.model.ScmDocDescriptor;
import ir.daneshrefah.scm.docs.client.model.ScmDocType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ScmDocsHtmlRendererTest {

    private final ScmDocsHtmlRenderer renderer = new ScmDocsHtmlRenderer();

    @Test
    void escapesHtmlAndRendersDocumentLinks() {
        ScmDocDescriptor descriptor = new ScmDocDescriptor(
                "api-guide",
                "API <Guide>",
                "Do not render <script>alert('x')</script>",
                ScmDocType.MARKDOWN,
                ScmDocCategory.API
        );

        String html = renderer.render("Docs & Support", "/docs/", List.of(descriptor));

        assertThat(html).contains("<title>Docs &amp; Support</title>");
        assertThat(html).contains("href=\"/docs/api/api-guide\"");
        assertThat(html).contains("API &lt;Guide&gt;");
        assertThat(html).contains("Do not render &lt;script&gt;alert(&#39;x&#39;)&lt;/script&gt;");
        assertThat(html).doesNotContain("<script>alert");
    }
}
