package ir.daneshrefah.scm.docs.client.provider;

import ir.daneshrefah.scm.docs.client.autoconfigure.ScmDocsProperties;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ClasspathScmDocContentProviderTest {

    @Test
    void rejectsPathTraversalDocIds() {
        ClasspathScmDocContentProvider provider = new ClasspathScmDocContentProvider(
                new ScmDocsProperties(),
                new DefaultResourceLoader()
        );

        assertThatThrownBy(() -> provider.findById("../secret"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("unsafe path characters");
    }

    @Test
    void rejectsPathTraversalClasspathLocations() {
        ScmDocsProperties properties = new ScmDocsProperties();
        ScmDocsProperties.Document document = new ScmDocsProperties.Document();
        document.setId("escape");
        document.setClasspathLocation("../secret.md");
        properties.getDocuments().add(document);

        ClasspathScmDocContentProvider provider = new ClasspathScmDocContentProvider(
                properties,
                new DefaultResourceLoader()
        );

        assertThatThrownBy(() -> provider.findById("escape"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("path traversal");
    }
}
