package ir.daneshrefah.scm.config.server.service;

import jakarta.annotation.PostConstruct;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.config.server.environment.JGitEnvironmentProperties;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class GitService {
    @Autowired
    JGitEnvironmentProperties jGitEnv;
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
        if (repoDir.exists()) {
            git = Git.open(repoDir);
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
