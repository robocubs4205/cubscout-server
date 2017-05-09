package com.robocubs4205.cubscout.model.scorecard;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.*;

@Entity
@Table(
        uniqueConstraints = @UniqueConstraint(columnNames = {"field","robotRole"})
)
public class ScoreWeight {
    @Id
    @GeneratedValue
    private long id;
    @ManyToOne(optional = false)
    @JoinColumn(name="field")
    private FieldSection field;

    @ManyToOne(optional = false)
    @JoinColumn(name="robotRole")
    @JsonIgnore
    private RobotRole robotRole;

    private float weight;

    public ScoreWeight(){}

    public FieldSection getField() {
        return field;
    }

    public void setField(FieldSection field) {
        this.field = field;
    }

    public float getWeight() {
        return weight;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public RobotRole getRobotRole() {
        return robotRole;
    }

    public void setRobotRole(RobotRole robotRole) {
        this.robotRole = robotRole;
    }
}
