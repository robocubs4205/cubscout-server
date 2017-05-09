package com.robocubs4205.cubscout.rest.v1;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.hateoas.ResourceSupport;

public class TeamResource extends ResourceSupport{
    private long id;
    private int number;
    private String name;
    private String gameType;
    public TeamResource(){}

    @JsonProperty("id")
    public long getTeamId() {
        return id;
    }

    public void setTeamId(long id) {
        this.id = id;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGameType() {
        return gameType;
    }

    public void setGameType(String gameType) {
        this.gameType = gameType;
    }
}
