package ir.daneshrefah.scm.uaa.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.FileSystemResource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UaaConfigurationArchitectureTest {
    private static final Path RESOURCES = Path.of("src/main/resources");
    private static final Path JAVA = Path.of("src/main/java/ir/daneshrefah/scm/uaa");

    @Test
    void onlyTheFiveAgreedApplicationFilesExist() throws Exception {
        Set<String> applicationFiles;
        try (var files = Files.list(RESOURCES)) {
            applicationFiles = files
                    .map(path -> path.getFileName().toString())
                    .filter(name -> name.startsWith("application-") || name.equals("application.yml"))
                    .filter(name -> name.endsWith(".yml"))
                    .collect(Collectors.toSet());
        }

        assertEquals(Set.of(
                "application.yml",
                "application-dev.yml",
                "application-test.yml",
                "application-pilot.yml",
                "application-prod.yml"
        ), applicationFiles);
        assertFalse(Files.exists(RESOURCES.resolve("application-default.yml")));
    }

    @Test
    void allApplicationFilesAreValidYaml() {
        for (String fileName : List.of(
                "application.yml",
                "application-dev.yml",
                "application-test.yml",
                "application-pilot.yml",
                "application-prod.yml"
        )) {
            YamlPropertiesFactoryBean yaml = new YamlPropertiesFactoryBean();
            yaml.setResources(new FileSystemResource(RESOURCES.resolve(fileName)));
            assertTrue(yaml.getObject() != null && !yaml.getObject().isEmpty(), fileName);
        }
    }

    @Test
    void localProfilesDoNotUseConfigServer() throws Exception {
        for (String fileName : List.of("application.yml", "application-dev.yml")) {
            String yaml = Files.readString(RESOURCES.resolve(fileName));
            assertTrue(yaml.contains("enabled: false"), () -> fileName + " must disable scm-config");
            assertFalse(yaml.contains("optional:configserver:"), () -> fileName + " must not import scm-config");
        }
    }

    @Test
    void kubernetesProfilesUseConfigServerStrictCorsAndVarLogApp() throws Exception {
        for (String profile : List.of("test", "pilot", "prod")) {
            String yaml = Files.readString(RESOURCES.resolve("application-" + profile + ".yml"));
            assertTrue(yaml.contains("optional:configserver:"), () -> profile + " must import scm-config");
            assertTrue(yaml.contains("enabled: true"), () -> profile + " must enable scm-config");
            assertTrue(yaml.contains("${SCM_APP_LOG_DIRECTORY:/var/log/app}"));
            assertTrue(yaml.contains("allowed-origin-patterns: ${SCM_UAA_CORS_ALLOWED_ORIGIN_PATTERNS:}"));
            assertFalse(yaml.contains("localhost:*"));
            assertFalse(yaml.contains("allowed-origin-patterns: \"*\""));
            assertFalse(yaml.contains("allowed-origin-patterns: '*'"));
        }
    }

    @Test
    void datasourceConfigurationHasOnlyMainAndActivationConcepts() throws Exception {
        String properties = Files.readString(JAVA.resolve("config/DataSourceConfigProperties.java"));
        String application = Files.readString(RESOURCES.resolve("application.yml"));

        assertTrue(properties.contains("prefix = \"scm.uaa.datasource\""));
        assertTrue(properties.contains("DatasourceProperties main"));
        assertTrue(properties.contains("DatasourceProperties activation"));
        assertFalse(properties.contains("authenticationDatasource"));
        assertFalse(application.contains("authenticationDatasource"));
        assertFalse(application.contains("scm.logging.datasource"));
    }

    @Test
    void activationBeanGraphIsConditionalAndNormalAuthenticationUsesOptionalProviders() throws Exception {
        String activationConfig = Files.readString(JAVA.resolve("config/ActivationDataSourceConfig.java"));
        String activationJpa = Files.readString(JAVA.resolve("config/ActivationJpaConfig.java"));
        assertTrue(activationConfig.contains("prefix = \"scm.uaa.datasource.activation\""));
        assertTrue(activationConfig.contains("havingValue = \"true\""));
        assertTrue(activationJpa.contains("@ConditionalOnBean(name = \"activationDataSource\")"));

        Path activationServices = JAVA.resolve("service/activation");
        try (var files = Files.walk(activationServices)) {
            for (Path file : files.filter(path -> path.toString().endsWith(".java")).toList()) {
                String source = Files.readString(file);
                if (source.contains("@Service") || source.contains("@Component")) {
                    assertTrue(source.contains("@ConditionalOnBean(name = \"activationDataSource\")"),
                            () -> file + " must be disabled with the activation datasource");
                }
            }
        }

        String authenticationFlow = Files.readString(JAVA.resolve(
                "security/authentication/UaaPasswordAuthenticationFlowService.java"
        ));
        String activationPolicy = Files.readString(JAVA.resolve("security/oauth2/policy/ActivationPolicy.java"));
        assertTrue(authenticationFlow.contains("ObjectProvider<PwaAuthenticationService>"));
        assertTrue(activationPolicy.contains("ObjectProvider<UserActivationAuthenticationService>"));
    }
}
