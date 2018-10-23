package net.reliqs.emonlight.commons.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class CollectionStoreToFile<E extends Serializable> {
    private static final Logger log = LoggerFactory.getLogger(CollectionStoreToFile.class);

    private String path;

    public CollectionStoreToFile(String path) {

        this.path = path;
    }

    public Collection<E> read(boolean removeFile) {
        List<E> result = new LinkedList<>();
        File f = new File(path);
        if (f.exists()) {
            boolean fail = false;
            try (
                    FileInputStream fi = new FileInputStream(f);
                    ObjectInputStream oi = new ObjectInputStream(fi)
            ) {
                do {
                    E e = (E) oi.readObject();
                    if (e != null) {
                        result.add(e);
                    } else {
                        break;
                    }
                } while (true);
            } catch (IOException | ClassNotFoundException e) {
                log.error("Error reading " + path, e);
                fail = true;
            }
            if (removeFile && !fail) {
                if (f.delete()) {
                    log.debug("deleted {}", f.getAbsolutePath());
                } else {
                    log.error("Unable to delete {}", f.getAbsolutePath());
                }
            }
        }
        return result;
    }

    public boolean write(Collection<E> data) {
        if (data == null) {
            return false;
        }
        File f = new File(path);
        try (
                FileOutputStream fo = new FileOutputStream(f);
                ObjectOutputStream oo = new ObjectOutputStream(fo)
        ) {
            for (E e : data) {
                oo.writeObject(e);
            }
            // write null sentinel
            oo.writeObject(null);
        } catch (IOException e) {
            log.error("Error writing " + path, e);
            return false;
        }
        log.debug("saved {}", f.getAbsolutePath());
        return true;
    }
}
