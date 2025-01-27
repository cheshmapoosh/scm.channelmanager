package ir.daneshrefah.scm.config.server.service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.springframework.cloud.config.server.environment.JGitEnvironmentProperties;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GitService {
    private final JGitEnvironmentProperties jGitEnvironmentProperties;
    private final Git git;

    public void commitAndPush(String message) {
        try {
            CredentialsProvider credentialsProvider = null;
            if (StringUtils.isNotBlank(jGitEnvironmentProperties.getUsername())) {
                credentialsProvider = new UsernamePasswordCredentialsProvider(jGitEnvironmentProperties.getUsername(),
                        jGitEnvironmentProperties.getPassword());
            }
            git.add().addFilepattern(".").call();
            git.commit().setMessage(message).call();
            git.push().setCredentialsProvider(credentialsProvider).call();
        } catch (GitAPIException e) {
            throw new RuntimeException(e.getMessage(), e);
        }

    }

}
