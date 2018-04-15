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
import java.util.stream.StreamSupport;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class FileRepositoryImplTest {

    private File file = new File("out/gitTest/settings.yml");

    @Before
    public void before() throws IOException {
        FileUtils.delete(file.getParentFile(), FileUtils.RECURSIVE + FileUtils.SKIP_MISSING);
        Files.createDirectory(Paths.get(file.getParent()));
        Files.copy(Paths.get("src/test/resources/settings.yml"), Paths.get(file.getPath()));
    }

    @Test
    public void initRepoTest() throws IOException, GitAPIException {
        //        File file = new File("src/test/resources/gitTest/settings.yml");
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
    public void diffTest() throws IOException {
        //        Files.copy(Paths.get("src/test/resources/application.yml"), Paths.get( "src/test/resources/gitTest/application.yml"));
        //        File file = new File("src/test/resources/gitTest/settings.yml");
        FileRepositoryImpl fr = new FileRepositoryImpl(file.getAbsolutePath());
        assertThat(fr.initRepo(), is(true));

        Files.write(Paths.get(file.getAbsolutePath()), "the text".getBytes(), StandardOpenOption.APPEND);
        String out = fr.diff();
        //        System.out.println(out);
        assertThat(out, is("diff --git a/settings.yml b/settings.yml\n" + "index 9575e2e..122fbfe 100644\n" + "--- a/settings.yml\n" +
                "+++ b/settings.yml\n" + "@@ -233,3 +233,4 @@\n" + "   sendRate: 0\n" + "   active: true\n" + " \n" + "+the text\n" +
                "\\ No newline at end of file\n"));
    }

    @Test
    public void commitWithDiffTest() throws IOException {
        Files.copy(Paths.get("src/test/resources/application.yml"), Paths.get(file.getParent(), "application.yml"));
        //        File file = new File("src/test/resources/gitTest/settings.yml");
        FileRepositoryImpl fr = new FileRepositoryImpl(file.getAbsolutePath());
        assertThat(fr.initRepo(), is(true));

        Files.write(Paths.get(file.getAbsolutePath()), "the text".getBytes(), StandardOpenOption.APPEND);
        String out = fr.commitWithDiff("my message");
        //        System.out.println(out);
        assertThat(out, is("my message\n--\ndiff --git a/settings.yml b/settings.yml\n" + "index 9575e2e..122fbfe 100644\n" + "--- a/settings.yml\n" +
                "+++ b/settings.yml\n" + "@@ -233,3 +233,4 @@\n" + "   sendRate: 0\n" + "   active: true\n" + " \n" + "+the text\n" +
                "\\ No newline at end of file\n"));
    }

    @Test
    public void commitTest() throws IOException, GitAPIException {
        Files.copy(Paths.get("src/test/resources/application.yml"), Paths.get(file.getParent(), "application.yml"));
        //        File file = new File("src/test/resources/gitTest/settings.yml");
        FileRepositoryImpl fr = new FileRepositoryImpl(file.getAbsolutePath());
        assertThat(fr.initRepo(), is(true));
        assertThat(fr.commit("not executed commit"), is(true));
        assertThat(fr.commit("not executed commit 2"), is(true));
        //        Thread.sleep(4000);
        Git git = Git.open(file.getParentFile());
        assertThat(git.log().addPath(file.getName()).call(), IsIterableWithSize.<RevCommit>iterableWithSize(1));
        Files.write(Paths.get(file.getAbsolutePath()), "the text".getBytes(), StandardOpenOption.APPEND);
        assertThat(fr.commit("append text"), is(true));
        assertThat(fr.commit("append text 2, ignored"), is(true));
        assertThat(git.log().addPath(file.getName()).all().call(), IsIterableWithSize.<RevCommit>iterableWithSize(2));
    }

    @Test
    public void checkoutAndCommitTest() throws IOException, GitAPIException {
        FileRepositoryImpl fr = new FileRepositoryImpl(file.getAbsolutePath());
        assertThat(fr.initRepo(), is(true));
        Git git = Git.open(file.getParentFile());
        Files.write(Paths.get(file.getAbsolutePath()), "the text".getBytes(), StandardOpenOption.APPEND);
        assertThat(fr.commit("append text"), is(true));
        assertThat(git.log().addPath(file.getName()).call(), IsIterableWithSize.<RevCommit>iterableWithSize(2));
        Iterable<RevCommit> revCommits = git.log().all().call();
        String name = StreamSupport.stream(revCommits.spliterator(), false).reduce((a, b) -> b).get().getName();
        System.out.println("commit name " + name);
        assertThat(fr.checkoutAndCommit(name), is(true));
        assertThat(git.log().addPath(file.getName()).all().call(), IsIterableWithSize.<RevCommit>iterableWithSize(3));
    }

}