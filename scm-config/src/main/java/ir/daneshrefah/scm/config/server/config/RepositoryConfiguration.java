package ir.daneshrefah.scm.config.server.config;

import ir.daneshrefah.scm.config.server.util.GitUtils;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.*;
import org.springframework.cloud.config.server.environment.MultipleJGitEnvironmentProperties;
import org.springframework.cloud.config.server.environment.MultipleJGitEnvironmentRepository;
import org.springframework.cloud.config.server.environment.MultipleJGitEnvironmentRepositoryFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.net.URI;
import java.nio.charset.StandardCharsets;

@Slf4j
@RequiredArgsConstructor
@Configuration(proxyBeanMethods = false)
public class RepositoryConfiguration {
    private final MultipleJGitEnvironmentProperties multipleJGitEnvironmentProperties;

    @PostConstruct
    public void initBareRepo() throws Exception {
        URI gitUri = new URI(multipleJGitEnvironmentProperties.getUri());
        String gitUriScheme = gitUri.getScheme();
        if (StringUtils.isEmpty(gitUriScheme) || StringUtils.equals(gitUriScheme, "file")) {
            File repoServerDir = new File(gitUri.getPath());
            if (!repoServerDir.exists()) {
                boolean mkdirs = repoServerDir.mkdirs();
                if (!mkdirs) {
                    throw new RuntimeException("Unable to create directory " + repoServerDir.getAbsolutePath());
                }
            }
            if (!FileUtils.isDirectory(repoServerDir)) {
                throw new RuntimeException("Not a directory " + repoServerDir.getAbsolutePath());
            }

            Git bareRepo = null;
            try {
                if (FileUtils.isEmptyDirectory(repoServerDir)) {
                    bareRepo = Git.init()
                            .setBare(true)
                            .setDirectory(repoServerDir)
                            .setInitialBranch(multipleJGitEnvironmentProperties.getDefaultLabel())
                            .call();
                } else {
                    bareRepo = Git.open(repoServerDir);
                }

                if (!GitUtils.isBareRepository(repoServerDir)) {
                    throw new RuntimeException("Not a valid bare git repository " + repoServerDir.getAbsolutePath());
                }

                Ref ref = bareRepo.getRepository().findRef("refs/heads/" + multipleJGitEnvironmentProperties.getDefaultLabel());
                if (ref == null) {
                    Repository repository= bareRepo.getRepository();
                    ObjectInserter inserter = repository.newObjectInserter();
                    ObjectId blobId = inserter.insert(Constants.OBJ_BLOB, "".getBytes(StandardCharsets.UTF_8));
                    TreeFormatter treeFormatter = new TreeFormatter();
                    treeFormatter.append("README.md", FileMode.REGULAR_FILE, blobId);
                    ObjectId treeId = inserter.insert(treeFormatter);
                    ObjectId headId = repository.resolve(Constants.HEAD);

                    // Create a new commit object
                    CommitBuilder commitBuilder = new CommitBuilder();
                    commitBuilder.setTreeId(treeId); // Set the tree
                    if (headId != null) {
                        commitBuilder.setParentId(headId);
                    }
                    commitBuilder.setAuthor(new PersonIdent("scm-config", ""));
                    commitBuilder.setCommitter(new PersonIdent("scm-config", ""));
                    commitBuilder.setMessage("Initial commit in bare repo");

                    // Insert the commit
                    ObjectId commitId = inserter.insert(commitBuilder);
                    inserter.flush();
                    inserter.close();

                    // Update the HEAD reference
                    RefUpdate refUpdate = repository.updateRef(Constants.HEAD);
                    refUpdate.setNewObjectId(commitId);
                    refUpdate.setForceUpdate(true);
                    refUpdate.update();
                    log.info("Committed to bare repo with commit ID: {}", commitId.name());
                }
            } finally {
                if (bareRepo != null) {
                    bareRepo.close();
                }
            }
        }
    }

    @Bean
    public MultipleJGitEnvironmentRepository defaultEnvironmentRepository(
            MultipleJGitEnvironmentRepositoryFactory gitEnvironmentRepositoryFactory) throws Exception {
        return gitEnvironmentRepositoryFactory.build(multipleJGitEnvironmentProperties);
    }

    @Bean
    public Git git(MultipleJGitEnvironmentRepository environmentRepository) throws Exception {
        return environmentRepository.getGitFactory().getGitByOpen(multipleJGitEnvironmentProperties.getBasedir());
    }

}
