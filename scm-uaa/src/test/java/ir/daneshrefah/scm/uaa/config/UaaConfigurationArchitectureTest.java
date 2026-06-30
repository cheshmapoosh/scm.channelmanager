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
    private static final Path MAIN_DATASOURCE = JAVA.resolve(
            "config/datasource/authentication/MainDataSourceConfig.java"
    );
    private static final Path ACTIVATION_DATASOURCE = JAVA.resolve(
            "config/datasource/activation/ActivationDataSourceConfig.java"
    );

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
    void legacyMbPwaActivationBeanGraphIsConditionalAndNormalAuthenticationUsesOptionalProviders() throws Exception {
        String activationConfig = Files.readString(ACTIVATION_DATASOURCE);
        assertTrue(activationConfig.contains("prefix = \"scm.uaa.datasource.activation\""));
        assertTrue(activationConfig.contains("havingValue = \"true\""));

        Path activationServices = JAVA.resolve("service/activation/pwa");
        try (var files = Files.walk(activationServices)) {
            for (Path file : files.filter(path -> path.toString().endsWith(".java")).toList()) {
                String source = Files.readString(file);
                if (source.contains("@Service") || source.contains("@Component")) {
                    assertTrue(source.contains("@ConditionalOnBean(name = \"activationDataSource\")"),
                            () -> file + " must be disabled with the activation datasource");
                }
            }
        }
        for (String relativePath : List.of(
                "controller/message/LoginMessageController.java",
                "controller/token/ActivationController.java",
                "service/messages/LoginMessageService.java",
                "security/oauth2/grant/legacy/response/LegacyPwaOauthLoginResponseProxyAdvisor.java"
        )) {
            String source = Files.readString(JAVA.resolve(relativePath));
            assertTrue(source.contains("@ConditionalOnBean(name = \"activationDataSource\")"),
                    () -> relativePath + " must be disabled with the activation datasource");
        }

        String authenticationFlow = Files.readString(JAVA.resolve(
                "security/authentication/UaaPasswordAuthenticationFlowService.java"
        ));
        String activationPolicy = Files.readString(JAVA.resolve("security/oauth2/policy/ActivationPolicy.java"));
        assertTrue(authenticationFlow.contains("ObjectProvider<PwaAuthenticationService>"));
        assertTrue(activationPolicy.contains("ObjectProvider<NibActivationEligibilityService>"));
    }

    @Test
    void nibActivationUsesOnlyTheMainDatasourceBoundary() throws Exception {
        Path nibServices = JAVA.resolve("service/activation/nib");
        try (var files = Files.walk(nibServices)) {
            for (Path file : files.filter(path -> path.toString().endsWith(".java")).toList()) {
                String source = Files.readString(file);
                if (source.contains("@Service") || source.contains("@Component")) {
                    assertTrue(source.contains("prefix = \"scm.uaa.activation.nib\""),
                            () -> file + " must use the NIB business feature toggle");
                }
                assertFalse(source.contains("activationDataSource"), () -> file + " depends on activationDataSource");
                assertFalse(source.contains("activationTransactionManager"),
                        () -> file + " uses activationTransactionManager");
                assertFalse(source.contains("PreAuthenticationToken"),
                        () -> file + " leaks a security token into NIB business code");
                assertFalse(source.contains("Map<String, Object>"),
                        () -> file + " manipulates raw database column maps");
            }
        }

        String orchestration = Files.readString(nibServices.resolve("NibActivationService.java"));
        assertTrue(orchestration.contains("transactionManager = \"mainTransactionManager\""));
        assertTrue(orchestration.contains("NibRoleProvisioningService"));
        assertTrue(orchestration.contains("NibChannelAuthenticationDuplicator"));
        assertTrue(orchestration.contains("NibMembershipAccessDuplicator"));

        Path nibRepository = JAVA.resolve("repository/authentication/nib/NibActivationJdbcRepository.java");
        String repository = Files.readString(nibRepository);
        assertTrue(Files.exists(nibRepository));
        assertFalse(Files.exists(JAVA.resolve("repository/activation/NibNativeRepository.java")));
        assertTrue(repository.contains("@Qualifier(\"mainJdbcTemplate\")"));
        assertTrue(repository.contains("@Qualifier(\"mainNamedParameterJdbcTemplate\")"));
        assertFalse(repository.contains("@Qualifier(\"activation"));
        assertPackageDoesNotContain(JAVA.resolve("repository/activation"), "Nib");

        String controller = Files.readString(JAVA.resolve("controller/activation/UserActivationController.java"));
        assertTrue(controller.contains("prefix = \"scm.uaa.activation.nib\""));
        assertFalse(controller.contains("activationDataSource"));

        String eligibility = Files.readString(nibServices.resolve("NibActivationEligibilityService.java"));
        assertFalse(eligibility.contains("UserService"));
        assertFalse(eligibility.contains("PreAuthenticationToken"));
        assertTrue(eligibility.contains("ActivationCandidateRequest"));
    }

    @Test
    void jdbcRepositoriesUseExplicitDatasourceTemplates() throws Exception {
        try (var files = Files.walk(JAVA.resolve("repository"))) {
            for (Path file : files
                    .filter(path -> path.getFileName().toString().endsWith("Repository.java"))
                    .toList()) {
                String source = Files.readString(file);
                if (source.contains("JdbcTemplate")) {
                    assertTrue(source.contains("@Qualifier(\"mainJdbcTemplate\")")
                                    || source.contains("@Qualifier(\"activationJdbcTemplate\")"),
                            () -> file + " has an unqualified JdbcTemplate");
                }
                if (source.contains("NamedParameterJdbcTemplate")) {
                    assertTrue(source.contains("@Qualifier(\"mainNamedParameterJdbcTemplate\")")
                                    || source.contains("@Qualifier(\"activationNamedParameterJdbcTemplate\")"),
                            () -> file + " has an unqualified NamedParameterJdbcTemplate");
                }
            }
        }
    }

    @Test
    void repositoryPackagesAreBoundToExactlyOnePersistenceUnit() throws Exception {
        String main = Files.readString(MAIN_DATASOURCE);
        String activation = Files.readString(ACTIVATION_DATASOURCE);

        assertTrue(main.contains(
                "basePackages = \"ir.daneshrefah.scm.uaa.repository.authentication\""
        ));
        assertTrue(main.contains("entityManagerFactoryRef = \"mainEntityManagerFactory\""));
        assertTrue(main.contains("transactionManagerRef = \"mainTransactionManager\""));
        assertFalse(main.contains("repository.activation"));
        assertFalse(main.contains("activationEntityManagerFactory"));
        assertFalse(main.contains("activationTransactionManager"));

        assertTrue(activation.contains(
                "basePackages = \"ir.daneshrefah.scm.uaa.repository.activation\""
        ));
        assertTrue(activation.contains("entityManagerFactoryRef = \"activationEntityManagerFactory\""));
        assertTrue(activation.contains("transactionManagerRef = \"activationTransactionManager\""));
        assertFalse(activation.contains("repository.authentication"));
        assertFalse(activation.contains("mainEntityManagerFactory"));
        assertFalse(activation.contains("mainTransactionManager"));

        long repositoryScans;
        try (var files = Files.walk(JAVA.resolve("config"))) {
            repositoryScans = files
                    .filter(path -> path.toString().endsWith(".java"))
                    .map(this::readUnchecked)
                    .filter(source -> source.contains("@EnableJpaRepositories"))
                    .count();
        }
        assertEquals(2, repositoryScans, "only the two datasource configs may scan repositories");
    }

    @Test
    void repositoriesDoNotReferenceTheOppositeTransactionManager() throws Exception {
        assertPackageDoesNotContain(
                JAVA.resolve("repository/authentication"),
                "activationTransactionManager"
        );
        assertPackageDoesNotContain(
                JAVA.resolve("repository/activation"),
                "mainTransactionManager"
        );
    }

    private String readUnchecked(Path path) {
        try {
            return Files.readString(path);
        } catch (Exception exception) {
            throw new IllegalStateException(exception);
        }
    }

    private void assertPackageDoesNotContain(Path packagePath, String forbiddenText) throws Exception {
        try (var files = Files.walk(packagePath)) {
            for (Path file : files.filter(path -> path.toString().endsWith(".java")).toList()) {
                assertFalse(Files.readString(file).contains(forbiddenText),
                        () -> file + " must not reference " + forbiddenText);
            }
        }
    }
}
