package com.robocubs4205.cubscout.rest.v1;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.hateoas.ResourceSupport;

/**
 * Created by trevor on 2/15/17.
 */
public class GameResource extends ResourceSupport {
    private long id;
    private String name;
    private String type;
    private int year;

    public GameResource() {
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("id")
    public long getGameId() {
        return id;
    }

    public void setGameId(long id) {
        this.id = id;
    }
}
