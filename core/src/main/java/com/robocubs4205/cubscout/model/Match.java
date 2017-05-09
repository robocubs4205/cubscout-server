package com.robocubs4205.cubscout.model;

import com.robocubs4205.cubscout.model.scorecard.Result;
import org.springframework.hateoas.Identifiable;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.groups.Default;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by trevor on 2/14/17.
 */
@Entity
public class Match implements Identifiable<Long> {
    @Id
    @GeneratedValue
    private long id;

    @NotNull
    @ManyToOne(optional = false)
    private Event event;

    @ManyToMany(cascade = {CascadeType.PERSIST,CascadeType.MERGE})
    @JoinTable(
            name = "match_robot",
            joinColumns = {@JoinColumn(name = "Match_id")},
            inverseJoinColumns = {@JoinColumn(name = "Robot_id")}
    )
    private Set<Robot> robots = new HashSet<>();

    @OneToMany(mappedBy = "match")
    private Set<Result> results = new HashSet<>();

    @NotNull(groups = {Default.class,Creating.class})
    private int number;

    @NotNull(groups = {Default.class,Creating.class})
    private String type;

    public Match(){}

    @Override
    public Long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public Set<Robot> getRobots() {
        return robots;
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

    public Set<Result> getResults() {
        return results;
    }

    public interface Creating{}

}
