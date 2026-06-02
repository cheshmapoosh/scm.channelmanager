package ir.daneshrefah.scm.docs.client.provider;

import ir.daneshrefah.scm.docs.client.autoconfigure.ScmDocsProperties;
import ir.daneshrefah.scm.docs.client.model.ScmDocContent;
import ir.daneshrefah.scm.docs.client.model.ScmDocType;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ClasspathScmDocContentProviderTest {

    @Test
    void readsExistingClasspathDocument() {
        ClasspathScmDocContentProvider provider = provider(propertiesWithDocument("sample", "sample.md"));

        Optional<ScmDocContent> content = provider.findById("sample");

        assertThat(content).isPresent();
        assertThat(new String(content.get().body(), StandardCharsets.UTF_8)).contains("# Sample Guide");
        assertThat(content.get().mediaType()).isEqualTo("text/markdown; charset=UTF-8");
        assertThat(content.get().fileName()).isEqualTo("sample.md");
    }

    @Test
    void returnsEmptyForMissingResource() {
        ClasspathScmDocContentProvider provider = provider(propertiesWithDocument("missing", "missing.md"));

        assertThat(provider.findById("missing")).isEmpty();
    }

    @Test
    void rejectsPathTraversalDocIds() {
        ClasspathScmDocContentProvider provider = provider(new ScmDocsProperties());

        assertThatThrownBy(() -> provider.findById("bad/id"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("unsafe path characters");
    }

    @Test
    void rejectsPathTraversalClasspathLocations() {
        ClasspathScmDocContentProvider provider = provider(propertiesWithDocument("escape", "../secret.md"));

        assertThatThrownBy(() -> provider.findById("escape"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("path traversal");
    }

    @Test
    void rejectsAbsoluteClasspathLocations() {
        ClasspathScmDocContentProvider provider = provider(propertiesWithDocument("absolute", "/secret.md"));

        assertThatThrownBy(() -> provider.findById("absolute"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("relative classpath path");
    }

    private ClasspathScmDocContentProvider provider(ScmDocsProperties properties) {
        return new ClasspathScmDocContentProvider(properties, new DefaultResourceLoader());
    }

    private ScmDocsProperties propertiesWithDocument(String id, String classpathLocation) {
        ScmDocsProperties properties = new ScmDocsProperties();
        properties.setModuleCode("scm-web");
        ScmDocsProperties.Document document = new ScmDocsProperties.Document();
        document.setId(id);
        document.setType(ScmDocType.MARKDOWN);
        document.setClasspathLocation(classpathLocation);
        document.setFileName(id + ".md");
        document.getTitle().put("en", "Sample Guide");
        properties.getDocuments().add(document);
        return properties;
    }
}
