package net.reliqs.emonlight.web.git;

import org.eclipse.jgit.revwalk.RevCommit;

import java.time.Instant;

public class CommitMessage {
    private String name;
    private String fullMessage;
    private String shortMessage;
    private Instant commitTime;

    public CommitMessage(RevCommit revCommit) {
        this.name = revCommit.getName();
        this.fullMessage = revCommit.getFullMessage();
        this.shortMessage = revCommit.getShortMessage();
        this.commitTime = Instant.ofEpochSecond(revCommit.getCommitTime());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFullMessage() {
        return fullMessage;
    }

    public void setFullMessage(String fullMessage) {
        this.fullMessage = fullMessage;
    }

    public String getShortMessage() {
        return shortMessage;
    }

    public void setShortMessage(String shortMessage) {
        this.shortMessage = shortMessage;
    }

    public Instant getCommitTime() {
        return commitTime;
    }

    public void setCommitTime(Instant commitTime) {
        this.commitTime = commitTime;
    }
}
