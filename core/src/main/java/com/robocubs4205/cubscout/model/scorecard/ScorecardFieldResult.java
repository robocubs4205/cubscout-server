package com.robocubs4205.cubscout.model.scorecard;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class ScorecardFieldResult {
    @Id
    @GeneratedValue
    private long id;

    @ManyToOne(optional=false)
    private FieldSection field;

    @JsonIgnore
    @ManyToOne(optional = false)
    private Result result;

    private Float score;

    public ScorecardFieldResult(){}

    public FieldSection getField() {
        return field;
    }

    public void setField(FieldSection field) {
        this.field = field;
    }

    public Float getScore() {
        return score;
    }

    public void setScore(Float score) {
        this.score = score;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Result getResult() {
        return result;
    }

    public void setResult(Result result) {
        this.result = result;
    }
}
