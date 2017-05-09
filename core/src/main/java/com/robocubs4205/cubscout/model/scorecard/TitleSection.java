package com.robocubs4205.cubscout.model.scorecard;

import javax.persistence.Entity;

/**
 * Created by trevor on 2/27/17.
 */
@Entity
public class TitleSection extends ScorecardSection {
    private String Title;

    public TitleSection() {
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }
}
