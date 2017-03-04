package com.robocubs4205.cubscout.model.scorecard;

import com.robocubs4205.cubscout.model.Match;
import com.robocubs4205.cubscout.model.Robot;
import com.robocubs4205.cubscout.model.scorecard.Scorecard;
import com.robocubs4205.cubscout.model.scorecard.ScorecardFieldResult;
import org.springframework.hateoas.Identifiable;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.groups.Default;
import java.util.List;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"match", "robot"}))
public class Result implements Identifiable<Long> {
    @Id
    @GeneratedValue
    private long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "match")
    private Match match;

    @ManyToOne(optional = false)
    @JoinColumn(name = "robot")
    @NotNull(groups = {Default.class, Creating.class})
    private Robot robot;

    @ManyToOne(optional = false)
    @NotNull(groups = {Default.class, Creating.class})
    private Scorecard scorecard;

    @OneToMany(mappedBy = "result")
    private List<ScorecardFieldResult> scores;

    public Result() {
    }

    public Match getMatch() {
        return match;
    }

    public void setMatch(Match match) {
        this.match = match;
    }

    public Robot getRobot() {
        return robot;
    }

    public void setRobot(Robot robot) {
        this.robot = robot;
    }

    public Long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public List<ScorecardFieldResult> getScores() {
        return scores;
    }

    public void setScores(List<ScorecardFieldResult> scores) {
        this.scores = scores;
    }

    public Scorecard getScorecard() {
        return scorecard;
    }

    public void setScorecard(Scorecard scorecard) {
        this.scorecard = scorecard;
    }

    public interface Creating {
    }
}