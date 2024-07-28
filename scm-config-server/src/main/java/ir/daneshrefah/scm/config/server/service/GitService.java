package ir.daneshrefah.scm.config.server.service;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class GitService {
    @Value("${spring.cloud.config.server.git.uri}")
    @Getter
    private String gitUri;
    @Value("${spring.cloud.config.server.git.basedir}")
    @Getter
    private String gitBaseDir;
    @Value("${spring.cloud.config.server.git.username:#{null}}")
    private String gitUsername;
    @Value("${spring.cloud.config.server.git.password:#{null}}")
    private String gitPassword;
    private CredentialsProvider credentialsProvider;
    private Git git;

    @PostConstruct
    public void loadGit() throws Exception {
        if (StringUtils.isNotEmpty(gitUsername)) {
            credentialsProvider = new UsernamePasswordCredentialsProvider(gitUsername, gitPassword);
        } else {
            credentialsProvider = null;
        }

        File repoDir = new File(gitBaseDir);
        if (repoDir.exists()) {
            git = Git.open(repoDir);
        } else {
            git = Git.cloneRepository()
                    .setURI(gitUri)
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
