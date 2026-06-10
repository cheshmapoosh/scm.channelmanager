package ir.daneshrefah.scm.docs.client.web;

import ir.daneshrefah.scm.docs.client.model.ScmApiDocGroupDescriptor;
import ir.daneshrefah.scm.docs.client.model.ScmApiDocItemDescriptor;
import ir.daneshrefah.scm.docs.client.model.ScmDocType;
import ir.daneshrefah.scm.docs.client.provider.ScmApiDocGroupCatalogProvider;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        classes = ScmDocsControllerEndpointTest.TestApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        properties = {
                "scm.docs.module-code=scm-web",
                "scm.docs.title.en=SCM Web Documentation",
                "scm.docs.title.fa=FA SCM Web Documentation",
                "scm.docs.documents[0].id=sample",
                "scm.docs.documents[0].module-code=scm-web",
                "scm.docs.documents[0].title.en=Sample Guide",
                "scm.docs.documents[0].title.fa=FA Sample Guide",
                "scm.docs.documents[0].description.en=Sample API documentation",
                "scm.docs.documents[0].type=MARKDOWN",
                "scm.docs.documents[0].category=GUIDE",
                "scm.docs.documents[0].classpath-location=sample.md",
                "scm.docs.documents[0].file-name=sample.md",
                "scm.docs.documents[0].order=10",
                "scm.docs.documents[1].id=openapi",
                "scm.docs.documents[1].module-code=scm-web",
                "scm.docs.documents[1].title.en=OpenAPI",
                "scm.docs.documents[1].description.en=OpenAPI specification",
                "scm.docs.documents[1].type=OPENAPI_JSON",
                "scm.docs.documents[1].category=API",
                "scm.docs.documents[1].classpath-location=openapi.json",
                "scm.docs.documents[1].file-name=openapi.json",
                "scm.docs.documents[1].order=20",
                "scm.docs.documents[2].id=unsafe-file",
                "scm.docs.documents[2].module-code=scm-web",
                "scm.docs.documents[2].title.en=Unsafe File",
                "scm.docs.documents[2].type=MARKDOWN",
                "scm.docs.documents[2].category=GUIDE",
                "scm.docs.documents[2].classpath-location=sample.md",
                "scm.docs.documents[2].file-name=../unsafe.md",
                "scm.docs.documents[2].order=30"
        }
)
@AutoConfigureMockMvc
class ScmDocsControllerEndpointTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void rendersHtmlDocsPage() throws Exception {
        mockMvc.perform(get("/docs").header("Accept-Language", "fa"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/html"))
                .andExpect(content().string(containsString("FA SCM Web Documentation")))
                .andExpect(content().string(containsString("FA Sample Guide")))
                .andExpect(content().string(containsString("href=\"/docs/api/sample\"")));
    }

    @Test
    void exposesJsonIndex() throws Exception {
        mockMvc.perform(get("/docs/api"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.documents").doesNotExist())
                .andExpect(jsonPath("$.groups[0].id").value("scm-web.channel-mb.100.card-inquiry.v1"))
                .andExpect(jsonPath("$.groups[0].documents[0].id").value("scm-web.channel-mb.100.card-inquiry.v1.OPENAPI_JSON.openapi-json"))
                .andExpect(jsonPath("$.groups[0].documents[0].href").value("/docs/api/scm-web.channel-mb.100.card-inquiry.v1.OPENAPI_JSON.openapi-json"))
                .andExpect(jsonPath("$.groups[0].documents[0].downloadUrl").doesNotExist());
    }

    @Test
    void exposesMarkdownDocContentDirectly() throws Exception {
        mockMvc.perform(get("/docs/api/sample"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", startsWith("text/markdown")))
                .andExpect(header().string("Content-Disposition", "inline; filename=\"sample.md\""))
                .andExpect(content().string(containsString("# Sample Guide")))
                .andExpect(content().string(not(containsString("\"descriptor\""))));
    }

    @Test
    void exposesOpenApiDocContentDirectly() throws Exception {
        mockMvc.perform(get("/docs/api/openapi"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(header().string("Content-Disposition", "inline; filename=\"openapi.json\""))
                .andExpect(jsonPath("$.openapi").value("3.0.0"))
                .andExpect(jsonPath("$.descriptor").doesNotExist());
    }

    @Test
    void sanitizesUnsafeFileNameInContentDisposition() throws Exception {
        mockMvc.perform(get("/docs/api/unsafe-file"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "inline; filename=\"unsafe.md\""));
    }

    @Test
    void returnsNotFoundForUnknownDocId() throws Exception {
        mockMvc.perform(get("/docs/api/missing"))
                .andExpect(status().isNotFound());
    }

    @Test
    void returnsBadRequestForUnsafeDocId() throws Exception {
        mockMvc.perform(get("/docs/api/bad..id"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid documentation request"));
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    static class TestApplication {
        @Bean
        ScmApiDocGroupCatalogProvider apiDocGroupCatalogProvider() {
            return () -> List.of(new ScmApiDocGroupDescriptor(
                    "scm-web.channel-mb.100.card-inquiry.v1",
                    "scm-web",
                    "channel.mb",
                    100L,
                    "card-inquiry",
                    "v1",
                    Map.of("en", "Card Inquiry API Docs"),
                    Map.of(),
                    List.of(new ScmApiDocItemDescriptor(
                            "scm-web.channel-mb.100.card-inquiry.v1.OPENAPI_JSON.openapi-json",
                            ScmDocType.OPENAPI_JSON,
                            Map.of("en", "Card Inquiry OpenAPI"),
                            Map.of(),
                            "application/json",
                            "openapi.json",
                            null,
                            10
                    )),
                    10
            ));
        }
    }
}
