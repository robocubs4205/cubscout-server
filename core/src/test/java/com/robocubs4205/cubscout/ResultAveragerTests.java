package com.robocubs4205.cubscout;

import com.robocubs4205.cubscout.model.scorecard.FieldSection;
import com.robocubs4205.cubscout.model.scorecard.Result;
import com.robocubs4205.cubscout.model.scorecard.ScorecardFieldResult;
import com.robocubs4205.cubscout.util.ResultUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@SuppressWarnings("OptionalGetWithoutIsPresent")
@RunWith(BlockJUnit4ClassRunner.class)
public class ResultAveragerTests {
    @Test
    public void ResultAverager_OneResult_OneField_SameNumberOfFields() {
        Set<Result> resultSet = new HashSet<>();
        Result result = new Result();
        ScorecardFieldResult fieldResult = new ScorecardFieldResult();
        FieldSection fieldSection = new FieldSection();
        fieldSection.setId(1);
        fieldResult.setField(fieldSection);
        fieldResult.setScore(12f);
        result.getScores().add(fieldResult);
        resultSet.add(result);

        Result newResult = resultSet.stream().collect(ResultUtil.resultAverager());

        assertTrue(newResult.getScores().size() == result.getScores().size());
    }

    @Test
    public void ResultAverager_OneResult_OneField_SameScoreAsOriginal() {
        Set<Result> resultSet = new HashSet<>();
        Result result = new Result();
        ScorecardFieldResult fieldResult = new ScorecardFieldResult();
        FieldSection fieldSection = new FieldSection();
        fieldSection.setId(1);
        fieldResult.setField(fieldSection);
        fieldResult.setScore(12f);
        result.getScores().add(fieldResult);
        resultSet.add(result);

        Result newResult = resultSet.stream().collect(ResultUtil.resultAverager());
        assertEquals(newResult.getScores().stream().findFirst().get().getScore(),
                     result.getScores().stream().findFirst().get().getScore(), 0.001);
    }

    @Test
    public void ResultAverager_TwoResults_Identical_OneField_SameNumberOfFields() {
        Set<Result> resultSet = new HashSet<>();
        Result result = new Result();
        Result result2 = new Result();
        ScorecardFieldResult fieldResult = new ScorecardFieldResult();
        FieldSection fieldSection = new FieldSection();
        fieldSection.setId(1);
        fieldResult.setField(fieldSection);
        fieldResult.setScore(12f);
        result.getScores().add(fieldResult);
        result2.getScores().add(fieldResult);
        resultSet.add(result);
        resultSet.add(result2);

        Result newResult = resultSet.stream().collect(ResultUtil.resultAverager());

        assertTrue(newResult.getScores().size() == result.getScores().size());
    }

    @Test
    public void ResultAverager_TwoResults_Identical_OneField_SameScoreAsOriginal() {
        Set<Result> resultSet = new HashSet<>();
        Result result = new Result();
        Result result2 = new Result();
        ScorecardFieldResult fieldResult = new ScorecardFieldResult();
        FieldSection fieldSection = new FieldSection();
        fieldSection.setId(1);
        fieldResult.setField(fieldSection);
        fieldResult.setScore(12f);
        result.getScores().add(fieldResult);
        result2.getScores().add(fieldResult);
        resultSet.add(result);
        resultSet.add(result2);

        Result newResult = resultSet.stream().collect(ResultUtil.resultAverager());
        assertEquals(newResult.getScores().stream().findFirst().get().getScore(),
                     result.getScores().stream().findFirst().get().getScore(), 0.001);
    }

    @Test
    public void ResultAverager_TwoResults_DifferentValues_OneField_IsAverage() {
        Set<Result> resultSet = new HashSet<>();
        Result result = new Result();
        Result result2 = new Result();
        ScorecardFieldResult fieldResult = new ScorecardFieldResult();
        ScorecardFieldResult fieldResult2 = new ScorecardFieldResult();
        FieldSection fieldSection = new FieldSection();
        fieldSection.setId(1);
        fieldResult.setField(fieldSection);
        fieldResult2.setField(fieldSection);
        fieldResult.setScore(12f);
        fieldResult2.setScore(10f);
        result.getScores().add(fieldResult);
        result2.getScores().add(fieldResult2);
        resultSet.add(result);
        resultSet.add(result2);

        Result newResult = resultSet.stream().collect(ResultUtil.resultAverager());
        assertEquals(newResult.getScores().stream().findFirst().get().getScore(), 11, 0.001);
    }

    @Test
    public void ResultAverager_OneResult_TwoFields_SameNumberOfFields() {
        Set<Result> resultSet = new HashSet<>();
        Result result = new Result();
        ScorecardFieldResult fieldResult = new ScorecardFieldResult();
        ScorecardFieldResult fieldResult2 = new ScorecardFieldResult();
        FieldSection fieldSection = new FieldSection();
        FieldSection fieldSection2 = new FieldSection();
        fieldSection.setId(1);
        fieldSection2.setId(2);
        fieldResult.setField(fieldSection);
        fieldResult2.setField(fieldSection2);
        fieldResult.setScore(12f);
        fieldResult2.setScore(3f);
        result.getScores().add(fieldResult);
        result.getScores().add(fieldResult2);
        resultSet.add(result);

        Result newResult = resultSet.stream().collect(ResultUtil.resultAverager());

        assertTrue(newResult.getScores().size() == result.getScores().size());
    }

    @Test
    public void ResultAverager_TwoResults_DifferentFields_TwoFieldsInResult() {
        Set<Result> resultSet = new HashSet<>();
        Result result = new Result();
        Result result2 = new Result();
        ScorecardFieldResult fieldResult = new ScorecardFieldResult();
        ScorecardFieldResult fieldResult2 = new ScorecardFieldResult();
        FieldSection fieldSection = new FieldSection();
        FieldSection fieldSection2 = new FieldSection();
        fieldSection.setId(1);
        fieldSection2.setId(2);
        fieldResult.setField(fieldSection);
        fieldResult2.setField(fieldSection2);
        fieldResult.setScore(12f);
        fieldResult2.setScore(10f);
        result.getScores().add(fieldResult);
        result2.getScores().add(fieldResult2);
        resultSet.add(result);
        resultSet.add(result2);

        Result newResult = resultSet.stream().collect(ResultUtil.resultAverager());

        assertTrue(newResult.getScores().size() == 2);
    }

    @Test
    public void ResultAverager_TwoResults_DifferentFields_SameScores() {
        Set<Result> resultSet = new HashSet<>();
        Result result = new Result();
        Result result2 = new Result();
        ScorecardFieldResult fieldResult = new ScorecardFieldResult();
        ScorecardFieldResult fieldResult2 = new ScorecardFieldResult();
        FieldSection fieldSection = new FieldSection();
        FieldSection fieldSection2 = new FieldSection();
        fieldSection.setId(1);
        fieldSection2.setId(2);
        fieldResult.setField(fieldSection);
        fieldResult2.setField(fieldSection2);
        fieldResult.setScore(12f);
        fieldResult2.setScore(10f);
        result.getScores().add(fieldResult);
        result2.getScores().add(fieldResult2);
        resultSet.add(result);
        resultSet.add(result2);

        Result newResult = resultSet.stream().collect(ResultUtil.resultAverager());

        newResult.getScores().forEach(scorecardFieldResult -> {
            assertEquals(scorecardFieldResult.getScore(),
                         Stream.concat(result.getScores().stream(), result2.getScores().stream())
                               .filter(scorecardFieldResult1 -> scorecardFieldResult.getField().getId() ==
                                       scorecardFieldResult1.getField().getId())
                               .findFirst().get().getScore(),
                         0.001);
        });
    }
}
