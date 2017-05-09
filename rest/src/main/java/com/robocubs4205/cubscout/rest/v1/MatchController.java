package com.robocubs4205.cubscout.rest.v1;

import com.robocubs4205.cubscout.model.*;
import com.robocubs4205.cubscout.model.scorecard.FieldSectionRepository;
import com.robocubs4205.cubscout.model.scorecard.Result;
import com.robocubs4205.cubscout.model.scorecard.ResultRepository;
import com.robocubs4205.cubscout.model.scorecard.ScorecardRepository;
import com.robocubs4205.cubscout.rest.JsonArrayContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

import static org.springframework.http.HttpHeaders.LOCATION;

@RestController
@RequestMapping(value = "/matches",produces = "application/vnd.robocubs-v1+json")
public class MatchController {
    private final MatchRepository matchRepository;
    private final ResultRepository resultRepository;
    private final RobotRepository robotRepository;

    private final ScorecardRepository scorecardRepository;
    private final FieldSectionRepository fieldSectionRepository;
    private final TeamRepository teamRepository;

    @Autowired
    public MatchController(MatchRepository matchRepository,
                           ResultRepository resultRepository,
                           RobotRepository robotRepository,
                           ScorecardRepository scorecardRepository,
                           FieldSectionRepository fieldSectionRepository,
                           TeamRepository teamRepository) {
        this.matchRepository = matchRepository;
        this.resultRepository = resultRepository;
        this.robotRepository = robotRepository;
        this.scorecardRepository = scorecardRepository;
        this.fieldSectionRepository = fieldSectionRepository;
        this.teamRepository = teamRepository;
    }

    @RequestMapping(method = RequestMethod.GET)
    public JsonArrayContainer<MatchResource> getAllMatches() {
        return new JsonArrayContainer<>(new MatchResourceAssembler()
                .toResources(matchRepository.findAll()));
    }

    @RequestMapping(value = "/{match:[0-9]+}", method = RequestMethod.GET)
    public MatchResource getMatch(@PathVariable Match match) {
        if (match == null)
            throw new ResourceNotFoundException("match does not exist");
        return new MatchResourceAssembler().toResource(match);
    }

    @RequestMapping(value = "/{match:[0-9]+}", method = RequestMethod.PUT)
    public MatchResource updateMatch(@PathVariable Match match,
                                     @RequestBody Match newMatch) {
        if (match == null)
            throw new ResourceNotFoundException("match does not exist");
        match.setNumber(newMatch.getNumber());
        matchRepository.saveAndFlush(match);
        return new MatchResourceAssembler().toResource(match);
    }

    @RequestMapping(value = "/{match:[0-9]+}", method = RequestMethod.DELETE)
    public void deleteMatch(@PathVariable Match match) {
        if (match == null)
            throw new ResourceNotFoundException("match does not exist");
        matchRepository.delete(match);
        matchRepository.flush();
    }

    @RequestMapping(value = "/{match:[0-9]+}/robots", method = RequestMethod.GET)
    public JsonArrayContainer<RobotResource> getAllRobots(@PathVariable Match match) {
        if (match == null)
            throw new ResourceNotFoundException("match does not exist");
        return new JsonArrayContainer<>(new RobotResourceAssembler().toResources(match.getRobots()));
    }

    @RequestMapping(value = "/{match:[0-9]+}/results", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED)
    public ResultResource createResult(@PathVariable Match match,
                                       @Validated(Result.Creating.class)
                                       @RequestBody Result result,
                                       HttpServletResponse response) {
        if (match == null) throw new ResourceNotFoundException();

        result.setScorecard(scorecardRepository
                .findById(result.getScorecard().getId()));
        if (result.getScorecard() == null) {
            throw new ScorecardDoesNotExistException();
        }

        result.setMatch(match);

        //remove null scores
        result.getScores().removeIf(
                fieldResult -> fieldResult.getScore() == null
        );

        //replace transient robot with entity from database
        Robot existingRobot = robotRepository
                .findByNumberAndGame(result.getRobot().getNumber(),result.getScorecard().getGame());
        if (existingRobot == null) { //create new robot
            //find team for this robot
            Team existingTeam = teamRepository
                    .findByNumberAndGameType(
                            result.getRobot().getNumber(),
                            result.getScorecard().getGame().getType());
            if (existingTeam == null) {
                Team team = new Team();
                team.setNumber(result.getRobot().getNumber());
                team.setGameType(result.getMatch().getEvent().getGame().getType());
                team.setDistrict(result.getMatch().getEvent().getDistrict());
                result.getRobot().setTeam(teamRepository.saveAndFlush(team));
            }
            else result.getRobot().setTeam(existingTeam);

            result.getRobot().setGame(result.getMatch().getEvent().getGame());

            result.setRobot(robotRepository.save(result.getRobot()));
        } else result.setRobot(existingRobot);

        //replace transient FieldSections with entities from database
        //todo: reduce database hits
        //noinspection ResultOfMethodCallIgnored
        result.getScores().stream()
              .peek(fieldResult -> fieldResult.setField(
                      fieldSectionRepository.findByIdAndScorecard(
                              fieldResult.getField().getId(),
                              result.getScorecard())))
              .peek(fieldResult -> {
                  if (fieldResult.getField() == null)
                      throw new ScoresDoNotExistException();
              })
              .forEach(fieldResult->fieldResult.setResult(result));


        if (!result.scoresMatchScorecardSections()) {
            throw new ScoresDoNotMatchScorecardException();
        }

        if (!result.allMissingScoresAreOptional()) {
            throw new RequiredScoresAbsentException();
        }

        if (!result.gameMatchesScorecard()) {
            throw new GameDoesNotMatchScorecardException();
        }

        match.getRobots().add(result.getRobot());
        result.getRobot().getMatches().add(match);
        result.setRobot(robotRepository.saveAndFlush(result.getRobot()));
        matchRepository.saveAndFlush(match);

        Result finalResult = resultRepository.saveAndFlush(result);

        ResultResource resultResource = new ResultResourceAssembler().toResource(result);
        response.setHeader(LOCATION,resultResource.getLink("self").getHref());
        return resultResource;
    }

    @RequestMapping(value = "/{match:[0-9]+}/results", method = RequestMethod.GET)
    public JsonArrayContainer<ResultResource> getAllResults(@PathVariable Match match) {
        if (match == null)
            throw new ResourceNotFoundException("match does not exist");
        return new JsonArrayContainer<>(new ResultResourceAssembler()
                .toResources(resultRepository.findByMatch(match)));
    }

    @ResponseStatus(value = HttpStatus.UNPROCESSABLE_ENTITY,
            reason = "The Scorecard specified does not exist")
    private class ScorecardDoesNotExistException extends RuntimeException {
    }

    //todo: allow detailed errors

    @ResponseStatus(value = HttpStatus.UNPROCESSABLE_ENTITY,
            reason = "The scores provided do not match the specified" +
                    " scorecard")
    private class ScoresDoNotMatchScorecardException extends RuntimeException {
    }

    @ResponseStatus(value = HttpStatus.UNPROCESSABLE_ENTITY,
            reason = "Some scores required by the specified " +
                    "scorecard are absent or null")
    private class RequiredScoresAbsentException extends RuntimeException {
    }

    @ResponseStatus(value = HttpStatus.UNPROCESSABLE_ENTITY,
            reason = "The Game for this match does not match the " +
                    "game for the specified scorecard")
    private class GameDoesNotMatchScorecardException extends RuntimeException {
    }

    @ResponseStatus(value = HttpStatus.UNPROCESSABLE_ENTITY,
            reason = "The scores provided do not match any scorecard")
    private class ScoresDoNotExistException extends RuntimeException {
    }
}
