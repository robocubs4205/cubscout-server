package com.robocubs4205.cubscout.model;

import org.springframework.hateoas.Identifiable;

import javax.persistence.*;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by trevor on 2/14/17.
 */
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"number","game"}))
public class Robot implements Identifiable<Long> {
    @Id
    @GeneratedValue
    private long id;

    @ManyToOne(optional = false)
    private Team team;

    @NotNull
    private int number;

    @NotNull
    private int year;

    private String name;
    @ManyToMany(cascade = CascadeType.PERSIST,mappedBy = "robots")
    private Set<Match> matches = new HashSet<>();

    @ManyToOne(optional = false)
    @JoinColumn(name="game")
    private Game game;

    public Robot(){}

    public Robot(long id){
        setId(id);
    }

    @AssertTrue
    public boolean numberMatchesTeam(){
        return number==team.getNumber();
    }

    public Long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    public Set<Match> getMatches() {
        return matches;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }
}
