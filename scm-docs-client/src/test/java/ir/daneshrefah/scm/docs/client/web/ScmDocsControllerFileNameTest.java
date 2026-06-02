package ir.daneshrefah.scm.docs.client.web;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ScmDocsControllerFileNameTest {

    @Test
    void keepsSafeFileNames() {
        assertThat(ScmDocsController.safeFileName("sample-guide.md")).isEqualTo("sample-guide.md");
    }

    @Test
    void sanitizesUnsafeFileNamesBeforeContentDisposition() {
        assertThat(ScmDocsController.safeFileName("../secret.md")).isEqualTo("secret.md");
        assertThat(ScmDocsController.safeFileName("dir\\secret.md")).isEqualTo("dir_secret.md");
        assertThat(ScmDocsController.safeFileName("bad\r\nInjected: yes.md")).isEqualTo("bad_Injected: yes.md");
        assertThat(ScmDocsController.safeFileName("bad\u0000name.md")).isEqualTo("bad_name.md");
    }
}
