package com.robocubs4205.cubscout.model.scorecard;

import com.robocubs4205.cubscout.model.Match;
import com.robocubs4205.cubscout.model.Robot;
import org.springframework.hateoas.Identifiable;

import javax.persistence.*;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;
import javax.validation.groups.Default;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Entity
public class Result implements Identifiable<Long> {
    @Id
    @GeneratedValue
    private long id;

    @ManyToOne(optional = false)
    private Match match;

    @ManyToOne(optional = false)
    @NotNull(groups = {Default.class, Creating.class})
    private Robot robot;

    @ManyToOne(optional = false)
    @NotNull(groups = {Default.class, Creating.class})
    private Scorecard scorecard;

    @OneToMany(mappedBy = "result",cascade = CascadeType.PERSIST)
    private Set<ScorecardFieldResult> scores = new HashSet<>();

    public Result() {
    }

    @AssertTrue
    public boolean scoresMatchScorecardSections() {
        return scores.isEmpty() || scores.stream().allMatch(score ->
                scorecard.getSections()
                         .stream()
                         .filter(section -> section instanceof FieldSection)
                         .map(section -> (FieldSection) section)
                         .anyMatch(fieldSection ->
                                 fieldSection.getId() ==
                                         score.getField().getId())
        );
    }

    @AssertTrue
    public boolean allMissingScoresAreOptional() {
        Supplier<Stream<FieldSection>> requiredSections = () ->
                scorecard.getSections().stream()
                         .filter(section -> section instanceof FieldSection)
                         .map(section -> (FieldSection) section)
                         .filter(fieldSection -> !fieldSection.isOptional());


        List<FieldSection> collect = requiredSections.get().collect(
                Collectors.toList());
        return collect.isEmpty()
                || requiredSections.get().noneMatch(
                fieldSection -> scores.stream()
                                      .noneMatch(scorecardFieldResult -> {
                                          return scorecardFieldResult.getField()
                                                                     .getId() ==
                                                  fieldSection.getId();
                                      })
        );
    }

    @AssertTrue
    public boolean gameMatchesScorecard() {
        return Objects.equals(match.getEvent().getGame().getId(),
                scorecard.getGame().getId());
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

    public Set<ScorecardFieldResult> getScores() {
        return scores;
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
