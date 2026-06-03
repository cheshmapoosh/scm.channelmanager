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
import ir.daneshrefah.scm.docs.client.model.ScmApiDocGroupDescriptor;
import ir.daneshrefah.scm.docs.client.model.ScmApiDocItemDescriptor;
import ir.daneshrefah.scm.docs.client.model.ScmDocContent;
import ir.daneshrefah.scm.docs.client.model.ScmDocType;
import ir.daneshrefah.scm.docs.client.autoconfigure.ScmDocsProperties;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.mock.env.MockEnvironment;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ScmWebRuntimeApiDocCatalogProviderTest {

    private final ChannelServiceDefinitionRepository repository = mock(ChannelServiceDefinitionRepository.class);
    private final ResourceLoader resourceLoader = mock(ResourceLoader.class);
    private final MockEnvironment environment = runtimeEnvironment();
    private final ScmWebRuntimeApiDocCatalogProvider provider = provider(environment);

    @Test
    void missingApiDocReturnsEmptyGroupsWhenFailFastFalse() {
        arrangeScopedDefinitions();

        assertThat(provider.findApiDocGroups()).isEmpty();
    }

    @Test
    void apiDocsCanBeDisabled() {
        ScmDocsProperties disabledProperties = docsProperties();
        disabledProperties.getApi().setEnabled(false);
        ScmWebRuntimeApiDocCatalogProvider disabledProvider = provider(runtimeEnvironment(), disabledProperties);

        assertThat(disabledProvider.findApiDocGroups()).isEmpty();
        assertThat(disabledProvider.findById("scm-web.channel-mb.100.card-inquiry.v1.OPENAPI_JSON.openapi-json"))
                .isEmpty();
        verify(repository, never()).findByTypeAndGatewayChannel_NameIn(
                ChannelServiceDefinitionType.API_DOC,
                List.of("channel.mb"));
    }

    @Test
    void createsOneGroupWithSortedDocumentItemsFromSingleApiDocDefinition() {
        arrangeScopedDefinitions(apiDocDefinition("api-doc-1", validDetailsWithThreeDocuments()));

        List<ScmApiDocGroupDescriptor> groups = provider.findApiDocGroups().stream().toList();

        assertThat(groups).hasSize(1);
        ScmApiDocGroupDescriptor group = groups.getFirst();
        assertThat(group.id()).isEqualTo("scm-web.channel-mb.100.card-inquiry.v1");
        assertThat(group.moduleCode()).isEqualTo("scm-web");
        assertThat(group.gatewayName()).isEqualTo("channel.mb");
        assertThat(group.channelServiceAccessId()).isEqualTo(100L);
        assertThat(group.serviceCode()).isEqualTo("card-inquiry");
        assertThat(group.version()).isEqualTo("v1");
        assertThat(group.title().get("en")).isEqualTo("Card Inquiry API Docs");
        assertThat(group.documents())
                .extracting(ScmApiDocItemDescriptor::id)
                .containsExactly(
                        "scm-web.channel-mb.100.card-inquiry.v1.OPENAPI_JSON.openapi-json",
                        "scm-web.channel-mb.100.card-inquiry.v1.WSDL.card-inquiry-wsdl",
                        "scm-web.channel-mb.100.card-inquiry.v1.MARKDOWN.guide-md"
                );
        assertThat(group.documents())
                .extracting(ScmApiDocItemDescriptor::docType)
                .containsExactly(ScmDocType.OPENAPI_JSON, ScmDocType.WSDL, ScmDocType.MARKDOWN);
    }

    @Test
    void detailsRefClasspathReadsExternalDetailsAndBuildsGroup() {
        arrangeScopedDefinitions(apiDocDefinition("api-doc-1", detailsRef("services/card/card-inquiry/v1/api-docs.json")));
        when(resourceLoader.getResource("classpath:services/card/card-inquiry/v1/api-docs.json"))
                .thenReturn(classpathResource(validDetailsWithThreeDocuments()));

        List<ScmApiDocGroupDescriptor> groups = provider.findApiDocGroups().stream().toList();

        assertThat(groups).hasSize(1);
        assertThat(groups.getFirst().version()).isEqualTo("v1");
        assertThat(groups.getFirst().documents())
                .extracting(ScmApiDocItemDescriptor::id)
                .containsExactly(
                        "scm-web.channel-mb.100.card-inquiry.v1.OPENAPI_JSON.openapi-json",
                        "scm-web.channel-mb.100.card-inquiry.v1.WSDL.card-inquiry-wsdl",
                        "scm-web.channel-mb.100.card-inquiry.v1.MARKDOWN.guide-md"
                );
    }

    @Test
    void missingDetailsRefIsSkippedWhenFailFastFalse() {
        arrangeScopedDefinitions(apiDocDefinition("api-doc-1", detailsRef("services/card/card-inquiry/v1/api-docs.json")));
        when(resourceLoader.getResource("classpath:services/card/card-inquiry/v1/api-docs.json"))
                .thenReturn(missingResource());

        assertThat(provider.findApiDocGroups()).isEmpty();
    }

    @Test
    void missingDetailsRefFailsWhenFailFastTrue() {
        ScmWebRuntimeApiDocCatalogProvider failFastProvider = provider(runtimeEnvironment(), failFastProperties());
        arrangeScopedDefinitions(apiDocDefinition("api-doc-1", detailsRef("services/card/card-inquiry/v1/api-docs.json")));
        when(resourceLoader.getResource("classpath:services/card/card-inquiry/v1/api-docs.json"))
                .thenReturn(missingResource());

        assertThatThrownBy(failFastProvider::findApiDocGroups)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("detailsRef resource is missing or unreadable");
    }

    @Test
    void unsafeDetailsRefPathIsSkippedWhenFailFastFalse() {
        arrangeScopedDefinitions(apiDocDefinition("api-doc-1", detailsRef("services/card/../api-docs.json")));

        assertThat(provider.findApiDocGroups()).isEmpty();
    }

    @Test
    void unsafeDetailsRefPathFailsWhenFailFastTrue() {
        ScmWebRuntimeApiDocCatalogProvider failFastProvider = provider(runtimeEnvironment(), failFastProperties());
        arrangeScopedDefinitions(apiDocDefinition("api-doc-1", detailsRef("services/card/../api-docs.json")));

        assertThatThrownBy(failFastProvider::findApiDocGroups)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must not contain path traversal");
    }

    @Test
    void invalidDetailsRefJsonIsSkippedWhenFailFastFalse() {
        arrangeScopedDefinitions(apiDocDefinition("api-doc-1", detailsRef("services/card/card-inquiry/v1/api-docs.json")));
        when(resourceLoader.getResource("classpath:services/card/card-inquiry/v1/api-docs.json"))
                .thenReturn(classpathResource("{"));

        assertThat(provider.findApiDocGroups()).isEmpty();
    }

    @Test
    void invalidDetailsRefJsonFailsWhenFailFastTrue() {
        ScmWebRuntimeApiDocCatalogProvider failFastProvider = provider(runtimeEnvironment(), failFastProperties());
        arrangeScopedDefinitions(apiDocDefinition("api-doc-1", detailsRef("services/card/card-inquiry/v1/api-docs.json")));
        when(resourceLoader.getResource("classpath:services/card/card-inquiry/v1/api-docs.json"))
                .thenReturn(classpathResource("{"));

        assertThatThrownBy(failFastProvider::findApiDocGroups)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("detailsRef resource must be valid JSON");
    }

    @Test
    void loadsClasspathContentByDocumentItemId() {
        arrangeScopedDefinitions(apiDocDefinition("api-doc-1", validDetailsWithThreeDocuments()));
        when(resourceLoader.getResource("classpath:services/card/card-inquiry/v1/openapi/openapi.json"))
                .thenReturn(new ByteArrayResource("{\"openapi\":\"3.0.0\"}".getBytes(StandardCharsets.UTF_8)));

        Optional<ScmDocContent> content = provider.findById(
                "scm-web.channel-mb.100.card-inquiry.v1.OPENAPI_JSON.openapi-json");

        assertThat(content).isPresent();
        assertThat(new String(content.get().body(), StandardCharsets.UTF_8)).isEqualTo("{\"openapi\":\"3.0.0\"}");
        assertThat(content.get().mediaType()).isEqualTo("application/json");
        assertThat(content.get().fileName()).isEqualTo("openapi.json");
        verify(resourceLoader).getResource("classpath:services/card/card-inquiry/v1/openapi/openapi.json");
    }

    @Test
    void invalidDocumentItemIsSkippedWhenFailFastFalse() {
        arrangeScopedDefinitions(apiDocDefinition("api-doc-1", detailsWithOneValidAndOneInvalidDocument()));

        List<ScmApiDocGroupDescriptor> groups = provider.findApiDocGroups().stream().toList();

        assertThat(groups).hasSize(1);
        assertThat(groups.getFirst().documents())
                .extracting(ScmApiDocItemDescriptor::id)
                .containsExactly("scm-web.channel-mb.100.card-inquiry.v1.OPENAPI_JSON.openapi-json");
    }

    @Test
    void invalidDocumentItemFailsWhenFailFastTrue() {
        ScmWebRuntimeApiDocCatalogProvider failFastProvider = provider(runtimeEnvironment(), failFastProperties());
        arrangeScopedDefinitions(apiDocDefinition("api-doc-1", detailsWithOneValidAndOneInvalidDocument()));

        assertThatThrownBy(failFastProvider::findApiDocGroups)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("name must not be blank");
    }

    @Test
    void invalidGroupIsSkippedWhenFailFastFalse() {
        arrangeScopedDefinitions(apiDocDefinition("api-doc-1", "{\"version\":\"v1\"}"));

        assertThat(provider.findApiDocGroups()).isEmpty();
    }

    @Test
    void invalidGroupFailsWhenFailFastTrue() {
        ScmWebRuntimeApiDocCatalogProvider failFastProvider = provider(runtimeEnvironment(), failFastProperties());
        arrangeScopedDefinitions(apiDocDefinition("api-doc-1", "{\"version\":\"v1\"}"));

        assertThatThrownBy(failFastProvider::findApiDocGroups)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("details must contain documents[]");
    }

    @Test
    void rejectsUnsafeLookupDocumentId() {
        assertThatThrownBy(() -> provider.findById("../secret"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("unsafe path characters");
    }

    @Test
    void queriesOnlyCurrentRuntimeGatewayNames() {
        arrangeScopedDefinitions(apiDocDefinition("api-doc-1", validDetailsWithThreeDocuments()));

        provider.findApiDocGroups();

        verify(repository).findByTypeAndGatewayChannel_NameIn(
                ChannelServiceDefinitionType.API_DOC,
                List.of("channel.mb"));
    }

    @Test
    void duplicateDocumentNamesAcrossExposuresDoNotCollide() {
        MockEnvironment multiTargetEnvironment = new MockEnvironment()
                .withProperty("scm.runtime.targets.channel.enabled", "true")
                .withProperty("scm.runtime.targets.channel.gateway-names[0]", "channel.mb")
                .withProperty("scm.runtime.targets.service-domain.enabled", "true")
                .withProperty("scm.runtime.targets.service-domain.gateway-names[0]", "domain.card");
        ScmWebRuntimeApiDocCatalogProvider multiTargetProvider = provider(multiTargetEnvironment);
        when(repository.findByTypeAndGatewayChannel_NameIn(
                ChannelServiceDefinitionType.API_DOC,
                List.of("channel.mb", "domain.card")))
                .thenReturn(List.of(
                        apiDocDefinition("api-doc-channel", "channel.mb", 100L, validDetailsWithThreeDocuments()),
                        apiDocDefinition("api-doc-domain", "domain.card", 200L, validDetailsWithThreeDocuments())));

        List<String> groupIds = multiTargetProvider.findApiDocGroups().stream()
                .map(ScmApiDocGroupDescriptor::id)
                .toList();

        assertThat(groupIds).containsExactly(
                "scm-web.channel-mb.100.card-inquiry.v1",
                "scm-web.domain-card.200.card-inquiry.v1");
    }

    @Test
    void brokenApiDocOutsideRuntimeTargetIsIgnored() {
        when(repository.findByType(ChannelServiceDefinitionType.API_DOC))
                .thenThrow(new IllegalStateException("broken API_DOC outside runtime target"));
        arrangeScopedDefinitions(apiDocDefinition("api-doc-1", validDetailsWithThreeDocuments()));

        assertThat(provider.findApiDocGroups()).hasSize(1);
        verify(repository, never()).findByType(ChannelServiceDefinitionType.API_DOC);
    }

    @Test
    void duplicateItemIdInsideSameGroupIsSkippedWhenFailFastFalse() {
        arrangeScopedDefinitions(apiDocDefinition("api-doc-1", detailsWithDuplicateDocumentItems()));

        List<ScmApiDocItemDescriptor> documents = provider.findApiDocGroups().stream()
                .findFirst()
                .orElseThrow()
                .documents();

        assertThat(documents).hasSize(1);
        assertThat(documents.getFirst().id())
                .isEqualTo("scm-web.channel-mb.100.card-inquiry.v1.OPENAPI_JSON.openapi-json");
    }

    @Test
    void duplicateItemIdInsideSameGroupFailsWhenFailFastTrue() {
        ScmWebRuntimeApiDocCatalogProvider failFastProvider = provider(runtimeEnvironment(), failFastProperties());
        arrangeScopedDefinitions(apiDocDefinition("api-doc-1", detailsWithDuplicateDocumentItems()));

        assertThatThrownBy(failFastProvider::findApiDocGroups)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("duplicate runtime API doc item id");
    }

    @Test
    void cacheEnabledQueriesRepositoryOnceForRepeatedGroupLookups() {
        arrangeScopedDefinitions(apiDocDefinition("api-doc-1", validDetailsWithThreeDocuments()));

        provider.findApiDocGroups();
        provider.findApiDocGroups();

        verify(repository, times(1)).findByTypeAndGatewayChannel_NameIn(
                ChannelServiceDefinitionType.API_DOC,
                List.of("channel.mb"));
    }

    @Test
    void findByIdUsesCachedCatalogAfterGroupLookup() {
        arrangeScopedDefinitions(apiDocDefinition("api-doc-1", validDetailsWithThreeDocuments()));
        when(resourceLoader.getResource("classpath:services/card/card-inquiry/v1/openapi/openapi.json"))
                .thenReturn(classpathResource("{\"openapi\":\"3.0.0\"}"));

        provider.findApiDocGroups();
        Optional<ScmDocContent> content = provider.findById(
                "scm-web.channel-mb.100.card-inquiry.v1.OPENAPI_JSON.openapi-json");

        assertThat(content).isPresent();
        verify(repository, times(1)).findByTypeAndGatewayChannel_NameIn(
                ChannelServiceDefinitionType.API_DOC,
                List.of("channel.mb"));
    }

    @Test
    void cacheDisabledQueriesRepositoryOnEachLookup() {
        ScmWebRuntimeApiDocCatalogProvider noCacheProvider = provider(runtimeEnvironment(), cacheDisabledProperties());
        arrangeScopedDefinitions(apiDocDefinition("api-doc-1", validDetailsWithThreeDocuments()));

        noCacheProvider.findApiDocGroups();
        noCacheProvider.findApiDocGroups();

        verify(repository, times(2)).findByTypeAndGatewayChannel_NameIn(
                ChannelServiceDefinitionType.API_DOC,
                List.of("channel.mb"));
    }

    @Test
    void invalidFailFastCatalogIsNotCached() {
        ScmWebRuntimeApiDocCatalogProvider failFastProvider = provider(runtimeEnvironment(), failFastProperties());
        arrangeScopedDefinitions(apiDocDefinition("api-doc-1", "{\"version\":\"v1\"}"));

        assertThatThrownBy(failFastProvider::findApiDocGroups)
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(failFastProvider::findApiDocGroups)
                .isInstanceOf(IllegalStateException.class);
        verify(repository, times(2)).findByTypeAndGatewayChannel_NameIn(
                ChannelServiceDefinitionType.API_DOC,
                List.of("channel.mb"));
    }

    @Test
    void providerDoesNotInjectEnvironmentForDocsApiConfiguration() {
        assertThat(Arrays.stream(ScmWebRuntimeApiDocCatalogProvider.class.getDeclaredFields())
                .map(field -> field.getType().getName())
                .toList())
                .doesNotContain("org.springframework.core.env.Environment");
    }

    private ScmWebRuntimeApiDocCatalogProvider provider(MockEnvironment environment) {
        return provider(environment, docsProperties());
    }

    private ScmWebRuntimeApiDocCatalogProvider provider(MockEnvironment environment, ScmDocsProperties docsProperties) {
        return new ScmWebRuntimeApiDocCatalogProvider(
                repository,
                new ObjectMapper(),
                resourceLoader,
                new ScmRuntimeProperties(environment),
                docsProperties);
    }

    private ScmDocsProperties docsProperties() {
        return new ScmDocsProperties();
    }

    private ScmDocsProperties failFastProperties() {
        ScmDocsProperties properties = docsProperties();
        properties.getApi().setFailFast(true);
        return properties;
    }

    private ScmDocsProperties cacheDisabledProperties() {
        ScmDocsProperties properties = docsProperties();
        properties.getApi().getCache().setEnabled(false);
        return properties;
    }

    private MockEnvironment runtimeEnvironment() {
        return new MockEnvironment()
                .withProperty("scm.runtime.gateway-name", "channel.mb");
    }

    private void arrangeScopedDefinitions(ChannelServiceDefinitionEntity... definitions) {
        when(repository.findByTypeAndGatewayChannel_NameIn(
                ChannelServiceDefinitionType.API_DOC,
                List.of("channel.mb")))
                .thenReturn(List.of(definitions));
    }

    private Resource classpathResource(String content) {
        return new ByteArrayResource(content.getBytes(StandardCharsets.UTF_8));
    }

    private Resource missingResource() {
        Resource resource = mock(Resource.class);
        when(resource.exists()).thenReturn(false);
        when(resource.isReadable()).thenReturn(false);
        return resource;
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

    private String detailsRef(String path) {
        return """
                {
                  "detailsRef": {
                    "type": "CLASSPATH",
                    "path": "%s"
                  }
                }
                """.formatted(path);
    }

    private String validDetailsWithThreeDocuments() {
        return """
                {
                  "version": "v1",
                  "title": {
                    "en": "Card Inquiry API Docs"
                  },
                  "order": 5,
                  "documents": [
                    {
                      "docType": "MARKDOWN",
                      "name": "guide.md",
                      "mediaType": "text/markdown; charset=UTF-8",
                      "source": {
                        "type": "CLASSPATH",
                        "path": "services/card/card-inquiry/v1/markdown/guide.md"
                      },
                      "order": 30
                    },
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

    private String detailsWithOneValidAndOneInvalidDocument() {
        return """
                {
                  "version": "v1",
                  "documents": [
                    {
                      "docType": "OPENAPI_JSON",
                      "name": "openapi.json",
                      "mediaType": "application/json",
                      "source": {
                        "type": "CLASSPATH",
                        "path": "services/card/card-inquiry/v1/openapi/openapi.json"
                      },
                      "order": 10
                    },
                    {
                      "docType": "WSDL",
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

    private String detailsWithDuplicateDocumentItems() {
        return """
                {
                  "version": "v1",
                  "documents": [
                    {
                      "docType": "OPENAPI_JSON",
                      "name": "openapi.json",
                      "mediaType": "application/json",
                      "source": {
                        "type": "CLASSPATH",
                        "path": "services/card/card-inquiry/v1/openapi/openapi.json"
                      }
                    },
                    {
                      "docType": "OPENAPI_JSON",
                      "name": "openapi.json",
                      "mediaType": "application/json",
                      "source": {
                        "type": "CLASSPATH",
                        "path": "services/card/card-inquiry/v1/openapi/openapi-v2.json"
                      }
                    }
                  ]
                }
                """;
    }
}
