package com.robocubs4205.cubscout.model.scorecard;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by trevor on 2/27/17.
 */
@Entity
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FieldSection extends ScorecardSection {
    @Enumerated(EnumType.STRING)
    @NotNull
    private FieldType type;

    @NotNull
    private String label;

    private NullWhen nullWhen;

    private String checkboxMessage;

    private boolean isOptional = false;

    @JsonIgnore
    @OneToMany(mappedBy = "field")
    private Set<ScorecardFieldResult> results = new HashSet<>();

    @JsonIgnore
    @OneToMany(mappedBy = "field",cascade = CascadeType.ALL)
    private Set<ScoreWeight> weights = new HashSet<>();

    public FieldSection() {
    }

    @JsonCreator
    public FieldSection(int id){
        setId(id);
    }

    public FieldType getType() {
        return type;
    }

    public void setType(FieldType type) {
        this.type = type;
    }

    public NullWhen getNullWhen() {
        return nullWhen;
    }

    public void setNullWhen(NullWhen nullWhen) {
        this.nullWhen = nullWhen;
    }

    public String getCheckboxMessage() {
        return checkboxMessage;
    }

    public void setCheckboxMessage(String checkboxMessage) {
        this.checkboxMessage = checkboxMessage;
    }

    public boolean isOptional() {
        return isOptional;
    }

    public void setOptional(boolean optional) {
        isOptional = optional;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Set<ScorecardFieldResult> getResults() {
        return results;
    }

    public Set<ScoreWeight> getWeights() {
        return weights;
    }

    public enum NullWhen {
        CHECKED, UNCHECKED
    }

    public enum FieldType{
        COUNT,RATING,BOOLEAN
    }
}
