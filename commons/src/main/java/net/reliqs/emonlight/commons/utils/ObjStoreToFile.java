package net.reliqs.emonlight.commons.utils;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

public class ObjStoreToFile<E extends Serializable> {

//    private static final Logger log = LoggerFactory.getLogger(ObjStoreToFile.class);

    private String path;
    private boolean removeFile;
    private List<E> data;

    public ObjStoreToFile(String path, boolean removeFile) {
        this.path = path;
        this.removeFile = removeFile;
        this.data = new LinkedList<>();
    }

    public boolean add(E obj) {
        return data.add(obj);
    }

    public boolean write() {
        CollectionStoreToFile<E> s = new CollectionStoreToFile<>(path);
        return s.write(data);
    }

    public List<E> read() {
        CollectionStoreToFile<E> s = new CollectionStoreToFile<>(path);
        return new LinkedList<>(s.read(removeFile));
    }

    public boolean isEmpty() {
        return data.isEmpty();
    }
}
