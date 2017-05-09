package com.robocubs4205.cubscout;

import com.robocubs4205.cubscout.model.scorecard.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.HashSet;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ResultEntityTests {
    @Mock
    private Scorecard scorecard;
    @Test
    public void scoresMatchScorecardSections_EmptyScoresAndScorecard_ReturnsTrue(){
        when(scorecard.getSections()).thenReturn(new HashSet<>());
        Result result = new Result();
        result.setScorecard(scorecard);
        assertTrue(result.scoresMatchScorecardSections());
    }
    @Test
    public void scoresMatchScorecardSections_EmptyScores_NonEmptyScorecard_ReturnsTrue(){
        HashSet<ScorecardSection> sections = new HashSet<>();
        FieldSection section = new FieldSection();
        section.setId(1);
        sections.add(section);
        when(scorecard.getSections()).thenReturn(sections);

        Result result = new Result();
        result.setScorecard(scorecard);
        assertTrue(result.scoresMatchScorecardSections());
    }
    @Test
    public void scoresMatchScorecardSections_NonEmptyScores_EmptyScorecard_ReturnsFalse(){
        when(scorecard.getSections()).thenReturn(new HashSet<>());

        Result result = new Result();
        result.setScorecard(scorecard);

        FieldSection section = new FieldSection();
        section.setId(1);
        ScorecardFieldResult fieldResult = new ScorecardFieldResult();
        fieldResult.setField(section);
        result.getScores().add(fieldResult);

        assertFalse(result.scoresMatchScorecardSections());
    }
    @Test
    public void scoresMatchScorecardSections_OneField_FieldsMatch_ReturnsTrue(){
        HashSet<ScorecardSection> sections = new HashSet<>();
        FieldSection section = new FieldSection();
        section.setId(1);
        sections.add(section);
        when(scorecard.getSections()).thenReturn(sections);

        Result result = new Result();
        result.setScorecard(scorecard);
        ScorecardFieldResult fieldResult = new ScorecardFieldResult();
        fieldResult.setField(section);
        result.getScores().add(fieldResult);

        assertTrue(result.scoresMatchScorecardSections());
    }
    @Test
    public void scoresMatchScorecardSections_OneField_FieldsDoNotMatch_ReturnsFalse(){
        HashSet<ScorecardSection> sections = new HashSet<>();
        FieldSection section = new FieldSection();
        section.setId(1);
        sections.add(section);
        when(scorecard.getSections()).thenReturn(sections);

        Result result = new Result();
        result.setScorecard(scorecard);
        ScorecardFieldResult fieldResult = new ScorecardFieldResult();
        FieldSection otherSection = new FieldSection();
        otherSection.setId(2);
        fieldResult.setField(otherSection);
        result.getScores().add(fieldResult);

        assertFalse(result.scoresMatchScorecardSections());
    }
    @Test
    public void scoresMatchScorecardSections_TwoFields_BothMatch_ReturnsTrue(){
        HashSet<ScorecardSection> sections = new HashSet<>();
        FieldSection section = new FieldSection();
        section.setId(1);
        sections.add(section);

        FieldSection section2 = new FieldSection();
        section2.setId(2);
        sections.add(section2);

        when(scorecard.getSections()).thenReturn(sections);

        Result result = new Result();
        result.setScorecard(scorecard);

        ScorecardFieldResult fieldResult = new ScorecardFieldResult();
        fieldResult.setField(section);
        result.getScores().add(fieldResult);

        ScorecardFieldResult fieldResult2 = new ScorecardFieldResult();
        fieldResult2.setField(section2);
        result.getScores().add(fieldResult2);

        assertTrue(result.scoresMatchScorecardSections());
    }
    @Test
    public void scoresMatchScorecardSections_TwoFields_OneMatch_ReturnsFalse(){
        HashSet<ScorecardSection> sections = new HashSet<>();
        FieldSection section = new FieldSection();
        section.setId(1);
        sections.add(section);

        FieldSection section2 = new FieldSection();
        section2.setId(2);
        sections.add(section2);

        when(scorecard.getSections()).thenReturn(sections);

        Result result = new Result();
        result.setScorecard(scorecard);

        ScorecardFieldResult fieldResult = new ScorecardFieldResult();
        fieldResult.setField(section);
        result.getScores().add(fieldResult);

        ScorecardFieldResult fieldResult2 = new ScorecardFieldResult();
        FieldSection otherSection = new FieldSection();
        otherSection.setId(3);
        fieldResult2.setField(otherSection);
        result.getScores().add(fieldResult2);

        assertFalse(result.scoresMatchScorecardSections());
    }
    @Test
    public void scoresMatchScorecardSections_ExtraScore_ReturnsFalse(){
        HashSet<ScorecardSection> sections = new HashSet<>();
        FieldSection section = new FieldSection();
        section.setId(1);
        sections.add(section);

        when(scorecard.getSections()).thenReturn(sections);

        Result result = new Result();
        result.setScorecard(scorecard);

        ScorecardFieldResult fieldResult = new ScorecardFieldResult();
        fieldResult.setField(section);
        result.getScores().add(fieldResult);

        ScorecardFieldResult fieldResult2 = new ScorecardFieldResult();
        FieldSection otherSection = new FieldSection();
        otherSection.setId(3);
        fieldResult2.setField(otherSection);
        result.getScores().add(fieldResult2);

        assertFalse(result.scoresMatchScorecardSections());
    }
    @Test
    public void scoresMatchScorecardSections_MissingScore_ReturnsTrue(){
        HashSet<ScorecardSection> sections = new HashSet<>();
        FieldSection section = new FieldSection();
        section.setId(1);
        sections.add(section);

        FieldSection section2 = new FieldSection();
        section2.setId(2);
        sections.add(section2);

        when(scorecard.getSections()).thenReturn(sections);

        Result result = new Result();
        result.setScorecard(scorecard);

        ScorecardFieldResult fieldResult = new ScorecardFieldResult();
        fieldResult.setField(section);
        result.getScores().add(fieldResult);

        assertTrue(result.scoresMatchScorecardSections());
    }

    @Test
    public void allMissingScoresAreOptional_EmptyScoresAndScorecard_ReturnsTrue(){
        when(scorecard.getSections()).thenReturn(new HashSet<>());
        Result result = new Result();
        result.setScorecard(scorecard);
        assertTrue(result.allMissingScoresAreOptional());
    }
    @Test
    public void allMissingScoresAreOptional_EmptyScores_NonEmptyScorecard_RequiredField_ReturnsFalse(){
        HashSet<ScorecardSection> sections = new HashSet<>();
        FieldSection section = new FieldSection();
        section.setId(1);
        sections.add(section);
        when(scorecard.getSections()).thenReturn(sections);

        Result result = new Result();
        result.setScorecard(scorecard);

        assertFalse(result.allMissingScoresAreOptional());
    }
    @Test
    public void allMissingScoresAreOptional_EmptyScores_NonEmptyScorecard_NonRequiredField_ReturnsTrue(){
        HashSet<ScorecardSection> sections = new HashSet<>();
        FieldSection section = new FieldSection();
        section.setId(1);
        section.setOptional(true);
        sections.add(section);
        when(scorecard.getSections()).thenReturn(sections);

        Result result = new Result();
        result.setScorecard(scorecard);

        assertTrue(result.allMissingScoresAreOptional());
    }
    @Test
    public void allMissingScoresAreOptional_NonEmptyScores_EmptyScorecard_ReturnsTrue(){
        when(scorecard.getSections()).thenReturn(new HashSet<>());

        Result result = new Result();
        result.setScorecard(scorecard);

        FieldSection section = new FieldSection();
        section.setId(1);
        ScorecardFieldResult fieldResult = new ScorecardFieldResult();
        fieldResult.setField(section);
        result.getScores().add(fieldResult);

        assertTrue(result.allMissingScoresAreOptional());
    }
    @Test
    public void allMissingScoresAreOptional_OneField_FieldsMatch_ReturnsTrue(){
        HashSet<ScorecardSection> sections = new HashSet<>();
        FieldSection section = new FieldSection();
        section.setId(1);
        sections.add(section);
        when(scorecard.getSections()).thenReturn(sections);

        Result result = new Result();
        result.setScorecard(scorecard);
        ScorecardFieldResult fieldResult = new ScorecardFieldResult();
        fieldResult.setField(section);
        result.getScores().add(fieldResult);

        assertTrue(result.allMissingScoresAreOptional());
    }
    @Test
    public void allMissingScoresAreOptional_OneField_FieldsDoNotMatch_ReturnsFalse(){
        HashSet<ScorecardSection> sections = new HashSet<>();
        FieldSection section = new FieldSection();
        section.setId(1);
        sections.add(section);
        when(scorecard.getSections()).thenReturn(sections);

        Result result = new Result();
        result.setScorecard(scorecard);
        ScorecardFieldResult fieldResult = new ScorecardFieldResult();
        FieldSection otherSection = new FieldSection();
        otherSection.setId(2);
        fieldResult.setField(otherSection);
        result.getScores().add(fieldResult);

        assertFalse(result.allMissingScoresAreOptional());
    }
    @Test
    public void allMissingScoresAreOptional_TwoFields_BothMatch_ReturnsTrue(){
        HashSet<ScorecardSection> sections = new HashSet<>();
        FieldSection section = new FieldSection();
        section.setId(1);
        sections.add(section);

        FieldSection section2 = new FieldSection();
        section2.setId(2);
        sections.add(section2);

        when(scorecard.getSections()).thenReturn(sections);

        Result result = new Result();
        result.setScorecard(scorecard);

        ScorecardFieldResult fieldResult = new ScorecardFieldResult();
        fieldResult.setField(section);
        result.getScores().add(fieldResult);

        ScorecardFieldResult fieldResult2 = new ScorecardFieldResult();
        fieldResult2.setField(section2);
        result.getScores().add(fieldResult2);

        assertTrue(result.allMissingScoresAreOptional());
    }
    @Test
    public void allMissingScoresAreOptional_TwoFields_OneMatch_ReturnsFalse(){
        HashSet<ScorecardSection> sections = new HashSet<>();
        FieldSection section = new FieldSection();
        section.setId(1);
        sections.add(section);

        FieldSection section2 = new FieldSection();
        section2.setId(2);
        sections.add(section2);

        when(scorecard.getSections()).thenReturn(sections);

        Result result = new Result();
        result.setScorecard(scorecard);

        ScorecardFieldResult fieldResult = new ScorecardFieldResult();
        fieldResult.setField(section);
        result.getScores().add(fieldResult);

        ScorecardFieldResult fieldResult2 = new ScorecardFieldResult();
        FieldSection otherSection = new FieldSection();
        otherSection.setId(3);
        fieldResult2.setField(otherSection);
        result.getScores().add(fieldResult2);

        assertFalse(result.allMissingScoresAreOptional());
    }
    @Test
    public void allMissingScoresAreOptional_ExtraScore_ReturnsTrue(){
        HashSet<ScorecardSection> sections = new HashSet<>();
        FieldSection section = new FieldSection();
        section.setId(1);
        sections.add(section);

        when(scorecard.getSections()).thenReturn(sections);

        Result result = new Result();
        result.setScorecard(scorecard);

        ScorecardFieldResult fieldResult = new ScorecardFieldResult();
        fieldResult.setField(section);
        result.getScores().add(fieldResult);

        ScorecardFieldResult fieldResult2 = new ScorecardFieldResult();
        FieldSection otherSection = new FieldSection();
        otherSection.setId(3);
        fieldResult2.setField(otherSection);
        result.getScores().add(fieldResult2);

        assertTrue(result.allMissingScoresAreOptional());
    }
    @Test
    public void allMissingScoresAreOptional_MissingScore_RequiredField_ReturnsFalse(){
        HashSet<ScorecardSection> sections = new HashSet<>();
        FieldSection section = new FieldSection();
        section.setId(1);
        sections.add(section);

        FieldSection section2 = new FieldSection();
        section2.setId(2);
        sections.add(section2);

        when(scorecard.getSections()).thenReturn(sections);

        Result result = new Result();
        result.setScorecard(scorecard);

        ScorecardFieldResult fieldResult = new ScorecardFieldResult();
        fieldResult.setField(section);
        result.getScores().add(fieldResult);

        assertFalse(result.allMissingScoresAreOptional());
    }
    @Test
    public void allMissingScoresAreOptional_MissingScore_NonRequiredField_ReturnsTrue(){
        HashSet<ScorecardSection> sections = new HashSet<>();
        FieldSection section = new FieldSection();
        section.setId(1);
        sections.add(section);

        FieldSection section2 = new FieldSection();
        section2.setId(2);
        section2.setOptional(true);
        sections.add(section2);

        when(scorecard.getSections()).thenReturn(sections);

        Result result = new Result();
        result.setScorecard(scorecard);

        ScorecardFieldResult fieldResult = new ScorecardFieldResult();
        fieldResult.setField(section);
        result.getScores().add(fieldResult);

        assertTrue(result.allMissingScoresAreOptional());
    }
}
