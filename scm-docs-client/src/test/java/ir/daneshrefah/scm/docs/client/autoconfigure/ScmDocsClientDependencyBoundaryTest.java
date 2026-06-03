package ir.daneshrefah.scm.docs.client.autoconfigure;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class ScmDocsClientDependencyBoundaryTest {

    @Test
    void buildDoesNotDependOnScmCacheClient() throws IOException {
        String buildFile = Files.readString(moduleRoot().resolve("build.gradle"));

        assertThat(buildFile).doesNotContain("scm-cache-client");
    }

    @Test
    void mainSourcesDoNotImportScmCacheClientOrCaffeine() throws IOException {
        Path mainSources = moduleRoot().resolve("src/main/java");

        try (var paths = Files.walk(mainSources)) {
            String combinedSources = paths
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .map(this::read)
                    .reduce("", (left, right) -> left + "\n" + right);

            assertThat(combinedSources)
                    .doesNotContain("ir.daneshrefah.scm.cache.client")
                    .doesNotContain("com.github.benmanes.caffeine");
        }
    }

    private String read(Path path) {
        try {
            return Files.readString(path);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not read source file " + path, exception);
        }
    }

    private Path moduleRoot() {
        Path current = Path.of("").toAbsolutePath();
        Path nestedModule = current.resolve("scm-docs-client");
        if (Files.exists(nestedModule.resolve("build.gradle"))) {
            return nestedModule;
        }
        return current;
    }
}
