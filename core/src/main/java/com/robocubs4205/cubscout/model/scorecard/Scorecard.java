package com.robocubs4205.cubscout.model.scorecard;

import com.robocubs4205.cubscout.model.Game;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.hateoas.Identifiable;

import javax.persistence.*;
import javax.validation.groups.Default;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Entity
public class Scorecard implements Identifiable<Long> {
    @Id
    @GeneratedValue
    private long id;

    @OneToOne(cascade = CascadeType.PERSIST, optional = false)
    private Game game;

    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.REMOVE},mappedBy = "scorecard")
    @NotEmpty(groups = {Default.class,Creating.class})
    private Set<ScorecardSection> sections = new HashSet<>();

    @OneToMany(mappedBy = "scorecard")
    private Set<Result> results = new HashSet<>();

    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.REMOVE},mappedBy = "scorecard")
    private Set<RobotRole> robotRoles = new HashSet<>();

    @OneToOne(cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
    private RobotRole defaultRole;

    public Scorecard() {
    }

    public Scorecard(long id){
        setId(id);
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public Set<ScorecardSection> getSections() {
        return sections;
    }

    public List<FieldSection> getFields() {
        return sections.stream().filter(section -> section instanceof FieldSection)
                       .map(section -> (FieldSection) section).collect(Collectors.toList());
    }

    @Override
    public Long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Set<Result> getResults() {
        return results;
    }

    public Set<RobotRole> getRobotRoles() {
        return robotRoles;
    }

    public Set<RobotRole> getRoles() {
        return robotRoles;
    }

    public RobotRole getDefaultRole() {
        return defaultRole;
    }

    public void setDefaultRole(RobotRole defaultRole) {
        this.defaultRole = defaultRole;
    }

    public interface Creating{

    }
}
