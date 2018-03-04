package net.reliqs.emonlight.xbeegw.state;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;

public class CollectionStoreToFileTest {

    @Before
    @After
    public void cleanUp() throws IOException {
        Files.deleteIfExists(Paths.get("test.dat"));
        Files.deleteIfExists(Paths.get("testdir"));
    }

    @Test
    public void write() throws IOException {
        CollectionStoreToFile s = new CollectionStoreToFile("test.dat");
        assertThat(Files.notExists(Paths.get("test.dat")), is(true));
        assertThat(s.write(null), is(false));

        List<Integer> d = new ArrayList<Integer>();
        assertThat(s.write(d), is(true));
        assertThat(Files.exists(Paths.get("test.dat")), is(true));
        assertThat(Files.size(Paths.get("test.dat")), is(5L));

        d.addAll(Arrays.asList(4, 7, 9, 22));
        assertThat(s.write(d), is(true));
        assertThat(Files.size(Paths.get("test.dat")), greaterThan(5L));
        s = new CollectionStoreToFile("tes/t.dat");
        assertThat(s.write(d), is(false));
    }

    @Test
    public void read() throws IOException {
        CollectionStoreToFile s = new CollectionStoreToFile("file_not_existing");
        assertThat(s.read(true), is(Collections.EMPTY_LIST));

        s = new CollectionStoreToFile("test.dat");
        List<Integer> d = new ArrayList<Integer>();
        d.addAll(Arrays.asList(4, 7, 9, 22));
        assertThat(s.write(d), is(true));
        assertThat(s.read(false), equalTo(d));
        assertThat(Files.exists(Paths.get("test.dat")), is(true));

        Files.setPosixFilePermissions(Paths.get("test.dat"), PosixFilePermissions.fromString("---------"));
        assertThat(s.read(true), is(Collections.EMPTY_LIST));
        assertThat(Files.exists(Paths.get("test.dat")), is(true));
        Files.setPosixFilePermissions(Paths.get("test.dat"), PosixFilePermissions.fromString("rw-rw-r--"));

//        assertThat(Files.createDirectory(Paths.get("testdir"), PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("r--r--r--"))), is(notNullValue()));
        assertThat(Files.createDirectory(Paths.get("testdir")), is(notNullValue()));
        assertThat(Files.move(Paths.get("test.dat"), Paths.get("testdir/test.dat")), is(notNullValue()));
        Files.setPosixFilePermissions(Paths.get("testdir"), PosixFilePermissions.fromString("r-xr-xr-x"));
        s = new CollectionStoreToFile("testdir/test.dat");
        assertThat(s.read(true), equalTo(d));
        assertThat(Files.exists(Paths.get("testdir/test.dat")), is(true));
        Files.setPosixFilePermissions(Paths.get("testdir"), PosixFilePermissions.fromString("rwxrwxr-x"));
        assertThat(s.read(true), equalTo(d));
        assertThat(Files.notExists(Paths.get("testdir/test.dat")), is(true));
    }
}