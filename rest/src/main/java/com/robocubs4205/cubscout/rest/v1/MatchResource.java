package com.robocubs4205.cubscout.rest.v1;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.hateoas.ResourceSupport;

/**
 * Created by trevor on 2/17/17.
 */
public class MatchResource extends ResourceSupport{
    private long id;
    private int number;
    private String type;

    public MatchResource() {
    }

    @JsonProperty("id")
    public long getMatchId() {
        return id;
    }

    public void setMatchId(long id) {
        this.id = id;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
