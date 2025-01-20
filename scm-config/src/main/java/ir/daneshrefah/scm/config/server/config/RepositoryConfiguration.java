package ir.daneshrefah.scm.config.server.config;

import ir.daneshrefah.scm.config.server.util.GitUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.springframework.cloud.config.server.environment.MultipleJGitEnvironmentProperties;
import org.springframework.cloud.config.server.environment.MultipleJGitEnvironmentRepository;
import org.springframework.cloud.config.server.environment.MultipleJGitEnvironmentRepositoryFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;

@Slf4j
@Configuration(proxyBeanMethods = false)
public class RepositoryConfiguration {

    @Bean
    public MultipleJGitEnvironmentRepository defaultEnvironmentRepository(
            MultipleJGitEnvironmentRepositoryFactory gitEnvironmentRepositoryFactory,
            MultipleJGitEnvironmentProperties environmentProperties) throws Exception {
        String uri = environmentProperties.getUri();
        File repoServerDir = new File(uri);
        if (!repoServerDir.exists()) {
            boolean mkdirs = repoServerDir.mkdirs();
            if (!mkdirs) {
                throw new RuntimeException("Unable to create directory " + repoServerDir.getAbsolutePath());
            }
        }
        if (!FileUtils.isDirectory(repoServerDir)) {
            throw new RuntimeException("Not a directory " + repoServerDir.getAbsolutePath());
        }
        if (FileUtils.isEmptyDirectory(repoServerDir)) {
            Git.init().setBare(true).setDirectory(repoServerDir).call();
        }

        if (!GitUtils.isBareRepository(repoServerDir)) {
            throw new RuntimeException("Not a valid bare git repository " + repoServerDir.getAbsolutePath());
        }

        return gitEnvironmentRepositoryFactory.build(environmentProperties);
    }


}
