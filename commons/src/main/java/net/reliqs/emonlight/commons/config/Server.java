package net.reliqs.emonlight.commons.config;

import org.hibernate.validator.constraints.URL;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Server implements Serializable {

    static final long serialVersionUID = 1L;

    @NotNull
    private String name;

    @URL
    @NotNull
    private String url;

    @Min(0)
    private int sendRate;

    @Valid
    private List<ServerMap> maps = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getSendRate() {
        return sendRate;
    }

    public void setSendRate(int sendRate) {
        this.sendRate = sendRate;
    }

    public List<ServerMap> getMaps() {
        return maps;
    }

    public void setMaps(List<ServerMap> maps) {
        this.maps = maps;
    }

    @Override
    public String toString() {
        return "Server [name=" + name + "]";
    }

}
