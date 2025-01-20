package ir.daneshrefah.scm.config.server.service;

import ir.daneshrefah.scm.config.server.util.GitUtils;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.config.server.environment.JGitEnvironmentProperties;
import org.springframework.cloud.config.server.environment.JGitEnvironmentRepository;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
@RequiredArgsConstructor
public class GitService {
    private final JGitEnvironmentProperties jGitEnv;
    private final JGitEnvironmentRepository jGitEnvRepository;
    private CredentialsProvider credentialsProvider;
    private Git git;

    @PostConstruct
    public void loadGit() throws Exception {
        if (StringUtils.isNotEmpty(jGitEnv.getUsername())) {
            credentialsProvider = new UsernamePasswordCredentialsProvider(jGitEnv.getUsername(), jGitEnv.getPassword());
        } else {
            credentialsProvider = null;
        }

        File repoDir = jGitEnv.getBasedir();
        if (!FileUtils.isEmptyDirectory(repoDir)) {
            if (!GitUtils.isGitRepository(repoDir)) {
                throw new RuntimeException("Repository is not git repo " + repoDir.getAbsolutePath());
            }
            if (GitUtils.isBareRepository(repoDir)) {
                throw new RuntimeException("Git repository must not be bare " + repoDir.getAbsolutePath());
            }
            git = jGitEnvRepository.getGitFactory().getGitByOpen(repoDir);
        } else {
            git = Git.cloneRepository()
                    .setURI(jGitEnv.getUri())
                    .setDirectory(repoDir)
                    .setCredentialsProvider(credentialsProvider)
                    .call();
        }
    }

    public void commitAndPush(String message) {
        try {
            git.add().addFilepattern(".").call();
            git.commit().setMessage(message).call();
            git.push().setCredentialsProvider(credentialsProvider).call();
        } catch (GitAPIException e) {
            throw new RuntimeException(e.getMessage(), e);
        }

    }

}
