package ir.daneshrefah.scm.config.server.util;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import java.io.File;

public class GitUtils {
    public static boolean isGitRepository(File gitDir) {
        // Check if the directory contains essential Git files
        if (!FileUtils.isDirectory(gitDir)) {
            return false;
        }
        boolean valid = new File(gitDir, "config").exists() && new File(gitDir, "HEAD").exists();
        if (!valid) {
            valid = new File(gitDir, ".git/config").exists() && new File(gitDir, ".git/HEAD").exists();
        }
        return valid;
    }

    public static boolean isBareRepository(File gitDir) {
        try {
            Repository repository = new FileRepositoryBuilder()
                    .readEnvironment()
                    .findGitDir(gitDir)
                    .build();

            boolean bare = repository.isBare();
            repository.close();
            return bare;
        } catch (Exception e) {
            return false;
        }
    }
}
