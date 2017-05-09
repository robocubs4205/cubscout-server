package com.robocubs4205.cubscout.model;

import org.springframework.hateoas.Identifiable;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by trevor on 2/14/17.
 */
@Entity
public class District implements Identifiable<String> {
    @NotNull
    @Id
    private String code;

    @NotNull
    private String name;

    @OneToMany(mappedBy = "district")
    private Set<Event> events = new HashSet<>();

    @OneToMany(mappedBy = "district")
    private Set<Team> teams = new HashSet<>();

    public District(){}

    public District(String code){
        setCode(code);
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getId() {
        return getCode();
    }

    public Set<Event> getEvents() {
        return events;
    }

    public Set<Team> getTeams() {
        return teams;
    }
}
