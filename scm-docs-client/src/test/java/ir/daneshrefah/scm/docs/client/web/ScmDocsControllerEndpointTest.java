package ir.daneshrefah.scm.docs.client.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        classes = ScmDocsControllerEndpointTest.TestApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        properties = {
                "scm.docs.documents[0].id=sample",
                "scm.docs.documents[0].title=Sample API",
                "scm.docs.documents[0].description=Sample API documentation",
                "scm.docs.documents[0].type=MARKDOWN",
                "scm.docs.documents[0].category=API",
                "scm.docs.documents[0].classpath-location=sample.md"
        }
)
@AutoConfigureMockMvc
class ScmDocsControllerEndpointTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void exposesJsonIndex() throws Exception {
        mockMvc.perform(get("/docs/api"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.documents[0].id").value("sample"))
                .andExpect(jsonPath("$.documents[0].title").value("Sample API"))
                .andExpect(jsonPath("$.documents[0].category").value("API"));
    }

    @Test
    void exposesDocContent() throws Exception {
        mockMvc.perform(get("/docs/api/sample"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.descriptor.id").value("sample"))
                .andExpect(jsonPath("$.content").value(containsString("# Sample API")))
                .andExpect(jsonPath("$.contentType").value("text/markdown"));
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    static class TestApplication {
    }
}
