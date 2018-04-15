package net.reliqs.emonlight.web.git;

import net.reliqs.emonlight.web.services.FileRepository;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Set;

public class FileRepositoryImpl implements FileRepository {
    private static final Logger log = LoggerFactory.getLogger(FileRepositoryImpl.class);

    private String path;

    public FileRepositoryImpl(String path) {
        this.path = path;
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
                log.debug("initial commit performed.");
            }
            return status.isClean();
        } catch (GitAPIException e) {
            log.error("error in git repository", e);
        }
        return false;
    }

    private <R> R gitTemplate(GitOperation<R, Git, File> action) {
        File f‌ile = new File(path);
        try (Git git = Git.open(f‌ile.getParentFile())) {
            R ret = action.accept(git, f‌ile);
            return ret;
        } catch (IOException | GitAPIException e) {
            log.error("error in git operation", e);
        }
        return null;
    }

    @Override
    public String diff() {
        String res = gitTemplate((git, file) -> {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            git.diff().setOutputStream(out).setPathFilter(PathFilter.create(file.getName())).call();
            return out.toString();
        });
        return res;
    }

    @Override
    public boolean commit(String message) {
        Boolean res = gitTemplate((git, file) -> {
            Status status = git.status().addPath(file.getName()).call();
            if (!status.isClean()) {
                git.add().addFilepattern(file.getName()).call();
                RevCommit commit = git.commit().setMessage(message).call();
                status = git.status().addPath(file.getName()).call();
            }
            return status.isClean();
        });
        return BooleanUtils.isTrue(res);
    }

    @Override
    public String commitWithDiff(String message) {
        String res = gitTemplate((git, file) -> {
            Status status = git.status().addPath(file.getName()).call();
            String commitMsg = null;
            if (!status.isClean()) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                git.diff().setOutputStream(out).setPathFilter(PathFilter.create(file.getName())).call();
                git.add().addFilepattern(file.getName()).call();
                commitMsg = StringUtils.isEmpty(message) ? out.toString() : message + "\n--\n" + out.toString();
                git.commit().setMessage(commitMsg).call();
                status = git.status().addPath(file.getName()).call();
            }
            return status.isClean() ? commitMsg : null;
        });
        return res;
    }

    @Override
    public Iterable<RevCommit> history(int skip, int maxCount) {
        Iterable<RevCommit> res = gitTemplate((git, file) -> git.log().addPath(file.getName()).setSkip(skip).setMaxCount(maxCount).call());
        return res;
    }

    @Override
    public boolean checkoutAndCommit(String refName) {
        Boolean res = gitTemplate((git, file) -> {
            git.checkout().addPath(file.getName()).setStartPoint(refName).call();
            String commitMsg = "restored version " + refName;
            git.commit().setMessage(commitMsg).call();
            Status status = git.status().addPath(file.getName()).call();
            return status.isClean();
        });
        return BooleanUtils.isTrue(res);
    }

    @FunctionalInterface
    public interface GitOperation<R, T, F> {
        R accept(T git, F file) throws GitAPIException;
    }
}
