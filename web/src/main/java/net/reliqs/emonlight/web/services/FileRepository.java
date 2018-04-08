package net.reliqs.emonlight.web.services;

import org.eclipse.jgit.revwalk.RevCommit;

public interface FileRepository {
    //    void initRepo();
    boolean commit(String message);

    Iterable<RevCommit> getHistory();
}
