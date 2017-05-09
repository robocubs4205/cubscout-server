package com.robocubs4205.cubscout.model.scorecard;

import javax.persistence.Entity;

/**
 * Created by trevor on 2/27/17.
 */
@Entity
public class ParagraphSection extends ScorecardSection {
    private String text;

    public ParagraphSection() {
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
