package com.robocubs4205.cubscout.rest.v1;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.hateoas.ResourceSupport;

/**
 * Created by trevor on 2/18/17.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RobotResource extends ResourceSupport{
    private long id;
    private int number;
    private int year;
    private String name;
    public RobotResource(){}

    @JsonProperty("id")
    public long getRobotId() {
        return id;
    }

    public void setRobotId(long id) {
        this.id = id;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
