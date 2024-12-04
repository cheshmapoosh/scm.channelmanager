package ir.daneshrefah.scm.config.server.config;


import lombok.Data;
import org.eclipse.jgit.api.Git;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.config.server.environment.MultipleJGitEnvironmentProperties;
import org.springframework.cloud.config.server.environment.MultipleJGitEnvironmentRepository;
import org.springframework.cloud.config.server.environment.MultipleJGitEnvironmentRepositoryFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;

@Component
@ConfigurationProperties(prefix = "scm.config")
@Data
public class ConfigConfiguration {
    private Map<String, Set<String>> applications;

}

@Configuration(proxyBeanMethods = false)
class RepositoryConfiguration {

    @Bean
    @DependsOn()
    public MultipleJGitEnvironmentRepository defaultEnvironmentRepository(
            MultipleJGitEnvironmentRepositoryFactory gitEnvironmentRepositoryFactory,
            MultipleJGitEnvironmentProperties environmentProperties) throws Exception {
        String uri = environmentProperties.getUri();
        Path path = Paths.get(uri);
        if (Files.isDirectory(path)) {
            File repoServerDir = new File(uri);
            if (!repoServerDir.exists()) {
                boolean mkdirs = repoServerDir.mkdirs();
                if (mkdirs) {
                    Git.init().setBare(true).setDirectory(repoServerDir).call();
                }
            }
        }

        return gitEnvironmentRepositoryFactory.build(environmentProperties);
    }

}

