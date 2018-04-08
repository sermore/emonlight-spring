package net.reliqs.emonlight.web.git;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.util.FileUtils;
import org.hamcrest.collection.IsIterableWithSize;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class FileRepositoryImplTest {

    @Before
    public void before() throws IOException {
        FileUtils.delete(new File("src/test/resources/.git"), FileUtils.RECURSIVE);
    }

    @Test
    public void initRepoTest() throws IOException, GitAPIException {
        File file = new File("src/test/resources/settings.yml");
        FileRepositoryImpl fr = new FileRepositoryImpl(file.getAbsolutePath());
        assertThat(fr.initRepo(), is(true));
        Git git = Git.open(file.getParentFile());
        assertThat(git.status().addPath(file.getName()).call().isClean(), is(true));
        assertThat(git.status().addPath(file.getName()).call().hasUncommittedChanges(), is(false));
        assertThat(git.log().addPath(file.getName()).call(), IsIterableWithSize.<RevCommit>iterableWithSize(1));
        assertThat(fr.initRepo(), is(true));
        assertThat(git.status().addPath(file.getName()).call().isClean(), is(true));
        assertThat(git.status().addPath(file.getName()).call().hasUncommittedChanges(), is(false));
        assertThat(git.log().addPath(file.getName()).call(), IsIterableWithSize.<RevCommit>iterableWithSize(1));
    }

    @Test
    public void commitTest() throws IOException, GitAPIException, InterruptedException {
        File file = new File("src/test/resources/settings.yml");
        FileRepositoryImpl fr = new FileRepositoryImpl(file.getAbsolutePath());
        assertThat(fr.initRepo(), is(true));
        assertThat(fr.commit("not executed commit"), is(true));
        assertThat(fr.commit("not executed commit 2"), is(true));
        Thread.sleep(4000);
        Git git = Git.open(file.getParentFile());
        assertThat(git.log().addPath(file.getName()).call(), IsIterableWithSize.<RevCommit>iterableWithSize(1));
        Files.write(Paths.get(file.getAbsolutePath()), "the text".getBytes(), StandardOpenOption.APPEND);
        assertThat(fr.commit("append text"), is(true));
        assertThat(fr.commit("append text 2, ignored"), is(true));
        assertThat(git.log().addPath(file.getName()).all().call(), IsIterableWithSize.<RevCommit>iterableWithSize(2));
    }
}