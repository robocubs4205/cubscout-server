package com.robocubs4205.cubscout.util;

import com.robocubs4205.cubscout.model.Match;
import com.robocubs4205.cubscout.model.Robot;
import com.robocubs4205.cubscout.model.scorecard.FieldSection;
import com.robocubs4205.cubscout.model.scorecard.Result;
import com.robocubs4205.cubscout.model.scorecard.Scorecard;
import com.robocubs4205.cubscout.model.scorecard.ScorecardFieldResult;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class ResultUtil {
    public static Collector<Result, ?, Result> resultAverager() {
        class Container {
            private final ConcurrentMap<Long, AtomicInteger> countMap = new ConcurrentHashMap<>();
            private final ConcurrentMap<Long, DoubleAdder> scoreMap = new ConcurrentHashMap<>();
            private final ConcurrentMap<Long, FieldSection> fieldMap = new ConcurrentHashMap<>();
        }
        BiConsumer<Container, Result> accumulator = (container, result) -> {
            //add values from result to existing values in container
            container.scoreMap.forEach((fieldId, score) -> {
                result.getScores().stream()
                      .filter(scorecardFieldResult -> scorecardFieldResult.getField().getId() == fieldId)
                      .peek(scorecardFieldResult -> {
                          container.fieldMap.putIfAbsent(fieldId, scorecardFieldResult.getField());
                      })
                      .filter(scorecardFieldResult -> scorecardFieldResult.getScore() != null)
                      .peek(scorecardFieldResult -> container.countMap.get(fieldId).incrementAndGet())
                      .forEach(scorecardFieldResult -> score.add(scorecardFieldResult.getScore()));
            });

            //get fields not in container
            Set<ScorecardFieldResult> missingResults =
                    result.getScores().stream().filter(scorecardFieldResult -> {
                        return container.fieldMap.keySet().stream()
                                                 .noneMatch(id -> scorecardFieldResult.getField().getId() == id);
                    }).collect(Collectors.toSet());

            //add fields to container
            missingResults.stream()
                          .peek(scorecardFieldResult -> container.countMap
                                  .putIfAbsent(scorecardFieldResult.getField().getId(), new AtomicInteger(1)))
                          .peek(scorecardFieldResult -> container.fieldMap
                                  .putIfAbsent(scorecardFieldResult.getField().getId(),
                                               scorecardFieldResult.getField()))
                          .forEach(scorecardFieldResult -> {
                              container.scoreMap.putIfAbsent(scorecardFieldResult.getField().getId(),
                                                             new DoubleAdder());
                              container.scoreMap.get(scorecardFieldResult.getField().getId())
                                                .add(scorecardFieldResult.getScore());
                          });
        };

        return Collector.of(Container::new, accumulator, (container, container2) -> {
            container.countMap.keySet().forEach(key -> container.countMap.get(key).addAndGet(
                    container.countMap.getOrDefault(key, new AtomicInteger(0)).get()));
            container2.countMap.keySet()
                               .forEach(key -> container.countMap.putIfAbsent(key, container2.countMap.get(key)));
            return container;
        }, container -> {
            Result result = new Result();

            container.scoreMap.forEach((fieldId, score) -> {
                ScorecardFieldResult fieldResult = new ScorecardFieldResult();
                fieldResult.setScore(score.floatValue() / container.countMap.keySet().stream()
                                                                            .filter(id -> Objects.equals(id, fieldId))
                                                                            .map(id -> container.countMap.get(id).get())
                                                                            .findFirst().get());
                fieldResult.setField(container.fieldMap.get(fieldId));
                result.getScores().add(fieldResult);
            });

            return result;
        });
    }

    public static Collector<ScorecardFieldResult, ?, Result> resultBuilder() {
        return Collector.of(Result::new,
                            (result, scorecardFieldResult) -> result.getScores().add(scorecardFieldResult),
                            (result, result2) -> {
                                Result result3 = new Result();
                                result3.getScores().addAll(result.getScores());
                                result3.getScores().addAll(result2.getScores());
                                return result3;
                            },
                            Collector.Characteristics.UNORDERED, Collector.Characteristics.IDENTITY_FINISH);
    }

    public static final BiFunction<Match, Scorecard, Set<Result>> resultsForScorecard =
            (match, scorecard) -> match.getResults().stream()
                                       .filter(result -> {
                                           return Objects
                                                   .equals(result.getScorecard().getId(), scorecard.getId());
                                       }).collect(Collectors.toSet());

    public static final BiFunction<Set<Result>, Robot, Set<Result>> resultsForRobot = (results, robot) -> {
        return results.stream().filter(result -> Objects
                .equals(result.getRobot().getId(), robot.getId()))
                      .collect(Collectors.toSet());
    };

    public static final BiFunction<Match,Scorecard, Set<Result>> averageResultsForMatch =
            (match,scorecard) -> match.getRobots().stream()
                          .map(robot -> {
                              Result result = resultsForRobot
                                      .apply(resultsForScorecard.apply(match,scorecard), robot)
                                      .stream().collect(ResultUtil.resultAverager());
                              result.setRobot(robot);
                              return result;
                          })
                          .peek(result -> result.setMatch(match))
                          .collect(Collectors.toSet());
}
