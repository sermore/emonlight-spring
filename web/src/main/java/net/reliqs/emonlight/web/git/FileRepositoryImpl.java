package net.reliqs.emonlight.web.git;

import net.reliqs.emonlight.web.services.FileRepository;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Set;

public class FileRepositoryImpl implements FileRepository {
    private static final Logger log = LoggerFactory.getLogger(FileRepositoryImpl.class);

    private String path;

    public FileRepositoryImpl(String path) {
        this.path = path;
    }

    void init() {
        initRepo();
    }

    boolean initRepo() {
        File f‌ile = new File(path);
        try (Git git = Git.init().setDirectory(f‌ile.getParentFile()).call()) {
            String filePath = f‌ile.getName();
            Status status = git.status().addPath(filePath).call();
            Set<String> untracked = status.getUntracked();
            if (untracked.contains(f‌ile.getName())) {
                git.add().addFilepattern(filePath).call();
                git.commit().setMessage("initial commit.").call();
                status = git.status().addPath(filePath).call();
            }
            return status.isClean();
        } catch (GitAPIException e) {
            log.error("error in git repository", e);
        }
        return false;
    }

    @Override
    public boolean commit(String message) {
        File f‌ile = new File(path);
        try (Git git = Git.open(f‌ile.getParentFile())) {
            Status status = git.status().addPath(f‌ile.getName()).call();
            if (!status.isClean()) {
                git.add().addFilepattern(f‌ile.getName()).call();
                RevCommit commit = git.commit().setMessage(message).call();
                status = git.status().addPath(f‌ile.getName()).call();
            }
            return status.isClean();
        } catch (IOException | GitAPIException e) {
            log.error("error in commit operation", e);
        }
        return false;
    }

    @Override
    public Iterable<RevCommit> getHistory() {
        File f‌ile = new File(path);
        try (Git git = Git.open(f‌ile.getParentFile())) {
            return git.log().addPath(f‌ile.getName()).call();
        } catch (IOException | GitAPIException e) {
            log.error("error in log operation", e);
        }
        return null;
    }

    public boolean checkout(String refName) {
        File f‌ile = new File(path);
        try (Git git = Git.open(f‌ile.getParentFile())) {
            Ref res = git.checkout().addPath(f‌ile.getName()).setStartPoint(refName).call();
            return res != null;
        } catch (IOException | GitAPIException e) {
            log.error("error in checkout operation", e);
        }
        return false;
    }
}
