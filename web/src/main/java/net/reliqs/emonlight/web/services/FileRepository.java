package net.reliqs.emonlight.web.services;

import org.eclipse.jgit.revwalk.RevCommit;

public interface FileRepository {
    String diff();
    boolean commit(String message);

    String commitWithDiff(String message);

    Iterable<RevCommit> history(int skip, int maxCount);

    boolean checkoutAndCommit(String refName);
}
