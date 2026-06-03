package ir.daneshrefah.scm.web.docs;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.data.entity.asset.ChannelServiceAccessEntity;
import ir.daneshrefah.scm.common.data.entity.asset.ServiceEntity;
import ir.daneshrefah.scm.common.data.entity.definition.DefinitionEntity;
import ir.daneshrefah.scm.common.model.gateway.ChannelServiceDefinitionType;
import ir.daneshrefah.scm.core.entity.gateway.ChannelServiceDefinitionEntity;
import ir.daneshrefah.scm.core.entity.gateway.GatewayChannelEntity;
import ir.daneshrefah.scm.core.integration.runtime.ScmRuntimeProperties;
import ir.daneshrefah.scm.core.repository.gateway.ChannelServiceDefinitionRepository;
import ir.daneshrefah.scm.docs.client.model.ScmDocContent;
import ir.daneshrefah.scm.docs.client.model.ScmDocDescriptor;
import ir.daneshrefah.scm.docs.client.model.ScmDocType;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.mock.env.MockEnvironment;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ScmWebRuntimeApiDocCatalogProviderTest {

    private final ChannelServiceDefinitionRepository repository = mock(ChannelServiceDefinitionRepository.class);
    private final ResourceLoader resourceLoader = mock(ResourceLoader.class);
    private final ScmWebRuntimeApiDocCatalogProvider provider = new ScmWebRuntimeApiDocCatalogProvider(
            repository,
            new ObjectMapper(),
            resourceLoader,
            new ScmRuntimeProperties(new MockEnvironment()
                    .withProperty("scm.runtime.gateway-name", "channel.mb"))
    );

    @Test
    void createsOneDescriptorPerDocumentItemFromSingleApiDocDefinition() {
        arrangeScopedDefinitions(apiDocDefinition("api-doc-1", validDetails()));

        List<ScmDocDescriptor> documents = provider.findAll().stream().toList();

        assertThat(documents)
                .extracting(ScmDocDescriptor::id)
                .containsExactly(
                        "scm-web.channel-mb.100.card-inquiry.v1.OPENAPI_JSON.openapi-json",
                        "scm-web.channel-mb.100.card-inquiry.v1.WSDL.card-inquiry-wsdl"
                );
        assertThat(documents)
                .extracting(ScmDocDescriptor::type)
                .containsExactly(ScmDocType.OPENAPI_JSON, ScmDocType.WSDL);
        assertThat(documents.getFirst().titleFor("en")).isEqualTo("Card Inquiry OpenAPI");
        assertThat(documents.getFirst().mediaType()).isEqualTo("application/json");
        assertThat(documents.getFirst().fileName()).isEqualTo("openapi.json");
        assertThat(documents.getFirst().serviceCode()).isEqualTo("card-inquiry");
        assertThat(documents.getFirst().version()).isEqualTo("v1");
    }

    @Test
    void loadsClasspathContentByStableDocumentId() {
        arrangeScopedDefinitions(apiDocDefinition("api-doc-1", validDetails()));
        when(resourceLoader.getResource("classpath:services/card/card-inquiry/v1/openapi/openapi.json"))
                .thenReturn(new ByteArrayResource("{\"openapi\":\"3.0.0\"}".getBytes(StandardCharsets.UTF_8)));

        Optional<ScmDocContent> content = provider.findById("scm-web.channel-mb.100.card-inquiry.v1.OPENAPI_JSON.openapi-json");

        assertThat(content).isPresent();
        assertThat(new String(content.get().body(), StandardCharsets.UTF_8)).isEqualTo("{\"openapi\":\"3.0.0\"}");
        assertThat(content.get().mediaType()).isEqualTo("application/json");
        assertThat(content.get().fileName()).isEqualTo("openapi.json");
        verify(resourceLoader).getResource("classpath:services/card/card-inquiry/v1/openapi/openapi.json");
    }

    @Test
    void rejectsMultipleApiDocRowsForSameGatewayAndAccessExposure() {
        ChannelServiceDefinitionEntity first = apiDocDefinition("api-doc-1", validDetails());
        ChannelServiceDefinitionEntity second = apiDocDefinition("api-doc-2", validDetails());
        arrangeScopedDefinitions(first, second);

        assertThatThrownBy(provider::findAll)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("at most one API_DOC definition is allowed");
    }

    @Test
    void rejectsMissingDocumentsArray() {
        arrangeScopedDefinitions(apiDocDefinition("api-doc-1", "{\"version\":\"v1\"}"));

        assertThatThrownBy(provider::findAll)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("details must contain documents[]");
    }

    @Test
    void rejectsEmptyDocumentsArray() {
        arrangeScopedDefinitions(apiDocDefinition("api-doc-1", "{\"version\":\"v1\",\"documents\":[]}"));

        assertThatThrownBy(provider::findAll)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("documents[] must not be empty");
    }

    @Test
    void rejectsMissingRequiredDocumentFields() {
        String details = """
                {
                  "version": "v1",
                  "documents": [
                    {
                      "docType": "OPENAPI_JSON",
                      "source": {
                        "type": "CLASSPATH",
                        "path": "services/card/card-inquiry/v1/openapi/openapi.json"
                      }
                    }
                  ]
                }
                """;
        arrangeScopedDefinitions(apiDocDefinition("api-doc-1", details));

        assertThatThrownBy(provider::findAll)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("name must not be blank");
    }

    @Test
    void rejectsUnsupportedSourceTypeInPhaseOne() {
        String details = validDetails().replace("\"type\": \"CLASSPATH\"", "\"type\": \"HTTP\"");
        arrangeScopedDefinitions(apiDocDefinition("api-doc-1", details));

        assertThatThrownBy(provider::findAll)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("unsupported API_DOC source.type HTTP");
    }

    @Test
    void rejectsSourcePathOutsideServicesTree() {
        String details = validDetails().replace(
                "services/card/card-inquiry/v1/openapi/openapi.json",
                "other/card/openapi.json");
        arrangeScopedDefinitions(apiDocDefinition("api-doc-1", details));

        assertThatThrownBy(provider::findAll)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("source.path must start with services/");
    }

    @Test
    void rejectsLegacyScmDocsSourcePath() {
        String details = validDetails().replace(
                "services/card/card-inquiry/v1/openapi/openapi.json",
                "scm-docs/card/openapi.json");
        arrangeScopedDefinitions(apiDocDefinition("api-doc-1", details));

        assertThatThrownBy(provider::findAll)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("source.path must not start with scm-docs/");
    }

    @Test
    void rejectsPathTraversalInSourcePath() {
        String details = validDetails().replace(
                "services/card/card-inquiry/v1/openapi/openapi.json",
                "services/card/../openapi.json");
        arrangeScopedDefinitions(apiDocDefinition("api-doc-1", details));

        assertThatThrownBy(provider::findAll)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("source.path must not contain path traversal");
    }

    @Test
    void rejectsUnsafeLookupDocumentId() {
        assertThatThrownBy(() -> provider.findById("../secret"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("unsafe path characters");
    }

    @Test
    void queriesOnlyCurrentRuntimeGatewayNames() {
        arrangeScopedDefinitions(apiDocDefinition("api-doc-1", validDetails()));

        provider.findAll();

        verify(repository).findByTypeAndGatewayChannel_NameIn(
                ChannelServiceDefinitionType.API_DOC,
                List.of("channel.mb"));
    }

    @Test
    void duplicateDocumentNamesAcrossExposuresDoNotCollide() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("scm.runtime.targets.channel.enabled", "true")
                .withProperty("scm.runtime.targets.channel.gateway-names[0]", "channel.mb")
                .withProperty("scm.runtime.targets.service-domain.enabled", "true")
                .withProperty("scm.runtime.targets.service-domain.gateway-names[0]", "domain.card");
        ScmWebRuntimeApiDocCatalogProvider provider = new ScmWebRuntimeApiDocCatalogProvider(
                repository,
                new ObjectMapper(),
                resourceLoader,
                new ScmRuntimeProperties(environment));
        when(repository.findByTypeAndGatewayChannel_NameIn(
                ChannelServiceDefinitionType.API_DOC,
                List.of("channel.mb", "domain.card")))
                .thenReturn(List.of(
                        apiDocDefinition("api-doc-channel", "channel.mb", 100L, validDetails()),
                        apiDocDefinition("api-doc-domain", "domain.card", 200L, validDetails())));

        List<String> docIds = provider.findAll().stream()
                .map(ScmDocDescriptor::id)
                .toList();

        assertThat(docIds).contains(
                "scm-web.channel-mb.100.card-inquiry.v1.OPENAPI_JSON.openapi-json",
                "scm-web.domain-card.200.card-inquiry.v1.OPENAPI_JSON.openapi-json");
    }

    @Test
    void brokenApiDocOutsideRuntimeTargetIsIgnored() {
        when(repository.findByType(ChannelServiceDefinitionType.API_DOC))
                .thenThrow(new IllegalStateException("broken API_DOC outside runtime target"));
        arrangeScopedDefinitions(apiDocDefinition("api-doc-1", validDetails()));

        assertThat(provider.findAll()).hasSize(2);
        verify(repository, never()).findByType(ChannelServiceDefinitionType.API_DOC);
    }

    private void arrangeScopedDefinitions(ChannelServiceDefinitionEntity... definitions) {
        when(repository.findByTypeAndGatewayChannel_NameIn(
                ChannelServiceDefinitionType.API_DOC,
                List.of("channel.mb")))
                .thenReturn(List.of(definitions));
    }

    private ChannelServiceDefinitionEntity apiDocDefinition(String id, String details) {
        return apiDocDefinition(id, "channel.mb", 100L, details);
    }

    private ChannelServiceDefinitionEntity apiDocDefinition(String id,
                                                            String gatewayName,
                                                            Long channelServiceAccessId,
                                                            String details) {
        DefinitionEntity definition = new DefinitionEntity();
        definition.setId("definition-" + id);
        definition.setName(id);
        definition.setTitle(id);
        definition.setDetails(details);

        ServiceEntity service = new ServiceEntity();
        service.setId((short) 10);
        service.setCode("card-inquiry");

        ChannelServiceAccessEntity access = new ChannelServiceAccessEntity();
        access.setId(channelServiceAccessId);
        access.setService(service);

        GatewayChannelEntity gatewayChannel = new GatewayChannelEntity();
        gatewayChannel.setId("gateway-" + gatewayName);
        gatewayChannel.setName(gatewayName);

        ChannelServiceDefinitionEntity apiDocDefinition = new ChannelServiceDefinitionEntity();
        apiDocDefinition.setId(id);
        apiDocDefinition.setType(ChannelServiceDefinitionType.API_DOC);
        apiDocDefinition.setDefinition(definition);
        apiDocDefinition.setChannelServiceAccess(access);
        apiDocDefinition.setGatewayChannel(gatewayChannel);
        return apiDocDefinition;
    }

    private String validDetails() {
        return """
                {
                  "version": "v1",
                  "documents": [
                    {
                      "docType": "OPENAPI_JSON",
                      "name": "openapi.json",
                      "title": {
                        "en": "Card Inquiry OpenAPI"
                      },
                      "mediaType": "application/json",
                      "source": {
                        "type": "CLASSPATH",
                        "path": "services/card/card-inquiry/v1/openapi/openapi.json"
                      },
                      "order": 10
                    },
                    {
                      "docType": "WSDL",
                      "name": "card-inquiry.wsdl",
                      "title": {
                        "en": "Card Inquiry WSDL"
                      },
                      "mediaType": "application/xml",
                      "source": {
                        "type": "CLASSPATH",
                        "path": "services/card/card-inquiry/v1/soap/card-inquiry.wsdl"
                      },
                      "order": 20
                    }
                  ]
                }
                """;
    }
}
