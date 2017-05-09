package com.robocubs4205.cubscout.model;

import org.springframework.hateoas.Identifiable;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.groups.Default;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
public class Event implements Identifiable<Long> {
    @Id
    @GeneratedValue
    private long id;

    @NotNull
    @ManyToOne(optional = false)
    private Game game;

    @NotNull(groups = {Default.class,Creating.class})
    private String shortName;

    private String address;

    @ManyToOne
    private District district;

    @OneToMany(mappedBy = "event")
    private Set<Match> matches = new HashSet<>();

    private Date startDate;

    private Date endDate;

    public Event() {
    }

    public Event(long id) {
        this.id = id;
    }

    @Override
    public Long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public District getDistrict() {
        return district;
    }

    public void setDistrict(District district) {
        this.district = district;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Set<Match> getMatches() {
        return matches;
    }

    public interface Creating{}
}
