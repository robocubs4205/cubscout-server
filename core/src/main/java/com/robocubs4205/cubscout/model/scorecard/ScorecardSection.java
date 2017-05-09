package com.robocubs4205.cubscout.model.scorecard;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

import static com.fasterxml.jackson.annotation.JsonSubTypes.Type;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "sectionType")
@JsonSubTypes({
        @Type(value = TitleSection.class, name = "title"),
        @Type(value = ParagraphSection.class, name = "paragraph"),
        @Type(value = FieldSection.class,name = "field")
})

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class ScorecardSection {
    @Id
    @GeneratedValue
    private long id;

    @NotNull
    private Integer index;


    @ManyToOne(optional = false)
    @JsonIgnore
    private Scorecard scorecard;

    public ScorecardSection(){}

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Scorecard getScorecard() {
        return scorecard;
    }

    public void setScorecard(Scorecard scorecard) {
        this.scorecard = scorecard;
    }
}
