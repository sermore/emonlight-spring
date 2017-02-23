package net.reliqs.emonlight.web.entities;

import org.springframework.boot.autoconfigure.web.ResourceProperties;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Created by sergio on 19/02/17.
 */
@Entity
public class Node {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
//    @NotNull
    private Long id;

    @NotNull
    @Size(min = 1)
    private String title;

    @NotNull
    @Size(min = 1)
    private String timeZone;

    @NotNull
    @Size(min = 4)
    private String authenticationToken;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    public String getAuthenticationToken() {
        return authenticationToken;
    }

    public void setAuthenticationToken(String authenticationToken) {
        this.authenticationToken = authenticationToken;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Node)) return false;

        Node node = (Node) o;

        if (id != null ? !id.equals(node.id) : node.id != null) return false;
        if (title != null ? !title.equals(node.title) : node.title != null) return false;
        if (timeZone != null ? !timeZone.equals(node.timeZone) : node.timeZone != null) return false;
        return authenticationToken != null ? authenticationToken.equals(node.authenticationToken) : node.authenticationToken == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + (timeZone != null ? timeZone.hashCode() : 0);
        result = 31 * result + (authenticationToken != null ? authenticationToken.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Node{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", timeZone='" + timeZone + '\'' +
                ", authenticationToken='" + authenticationToken + '\'' +
                '}';
    }
}
