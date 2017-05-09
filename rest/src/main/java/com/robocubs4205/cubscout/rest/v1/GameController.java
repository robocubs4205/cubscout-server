package com.robocubs4205.cubscout.rest.v1;

import com.robocubs4205.cubscout.rest.JsonArrayContainer;
import com.robocubs4205.cubscout.model.*;
import com.robocubs4205.cubscout.model.scorecard.*;
import com.robocubs4205.cubscout.util.ResultUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static com.robocubs4205.cubscout.util.ResultUtil.averageResultsForMatch;
import static com.robocubs4205.cubscout.util.ResultUtil.resultsForRobot;
import static org.springframework.http.HttpHeaders.LOCATION;

@RestController
@RequestMapping(value = "/games", produces = "application/vnd.robocubs-v1+json")
public class GameController {
    private final GameRepository gameRepository;
    private final EventRepository eventRepository;
    private final DistrictRepository districtRepository;
    private final ScorecardRepository scorecardRepository;
    private final ScorecardSectionRepository scorecardSectionRepository;

    @Autowired
    public GameController(GameRepository gameRepository,
                          EventRepository eventRepository,
                          DistrictRepository districtRepository,
                          ScorecardRepository scorecardRepository,
                          ScorecardSectionRepository scorecardSectionRepository) {
        this.gameRepository = gameRepository;
        this.eventRepository = eventRepository;
        this.districtRepository = districtRepository;
        this.scorecardRepository = scorecardRepository;
        this.scorecardSectionRepository = scorecardSectionRepository;
    }

    @RequestMapping(method = RequestMethod.GET)
    public JsonArrayContainer<GameResource> getAllGames() {
        return new JsonArrayContainer<>(new GameResourceAssembler()
                .toResources(gameRepository.findAll()));
    }

    @RequestMapping(value = "/{game:[0-9]+}", method = RequestMethod.GET)
    public GameResource getGame(@PathVariable Game game) {
        if (game == null)
            throw new ResourceNotFoundException("game does not exist");
        return new GameResourceAssembler().toResource(game);
    }

    @RequestMapping(method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED)
    public GameResource createGame(@Valid @RequestBody Game game, HttpServletResponse response) {
        game = gameRepository.saveAndFlush(game);
        GameResource gameResource = new GameResourceAssembler().toResource(game);
        response.setHeader(LOCATION, gameResource.getLink("self").getHref());
        return gameResource;
    }

    @RequestMapping(value = "/{game:[0-9]+}", method = RequestMethod.PUT)
    public GameResource updateGame(@PathVariable Game game,
                                   @Valid @RequestBody Game newGame) {
        if (game == null)
            throw new ResourceNotFoundException("game does not exist");
        game.setName(newGame.getName());
        game.setYear(newGame.getYear());
        game.setType(newGame.getType());
        game = gameRepository.saveAndFlush(game);
        return new GameResourceAssembler().toResource(game);
    }

    @RequestMapping(value = "/{game:[0-9]+}", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteGame(@PathVariable Game game) {
        if (game == null)
            throw new ResourceNotFoundException("game does not exist");
        gameRepository.delete(game);
        gameRepository.flush();
    }

    @RequestMapping(value = "/{game:[0-9]+}/events", method = RequestMethod.GET)
    public JsonArrayContainer<EventResource> getAllEvents(@PathVariable Game game) {
        if (game == null)
            throw new ResourceNotFoundException("game does not exist");
        return new JsonArrayContainer<>(new EventResourceAssembler().toResources(eventRepository.findByGame(game)));
    }

    @RequestMapping(value = "/{game:[0-9]+}/events", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public EventResource createEvent(@PathVariable Game game,
                                     @Validated(Event.Creating.class) @RequestBody Event event,
                                     HttpServletResponse response) {
        if (game == null)
            throw new ResourceNotFoundException("game does not exist");
        event.setGame(game);
        if (event.getDistrict() != null) {
            event.setDistrict(districtRepository
                                      .findByCode(event.getDistrict().getCode()));
            if (event.getDistrict() == null)
                throw new DistrictDoesNotExistException();
        }
        event = eventRepository.saveAndFlush(event);
        EventResource eventResource = new EventResourceAssembler().toResource(event);
        response.setHeader(LOCATION, eventResource.getLink("self").getHref());
        return eventResource;
    }

    @RequestMapping(value = "/{game:[0-9]+}/scorecards", method = RequestMethod.GET)
    public JsonArrayContainer<ScorecardResource> getAllScorecards(@PathVariable Game game) {
        if (game == null)
            throw new ResourceNotFoundException("game does not exist");
        if (game.getScorecard() == null) return new JsonArrayContainer<>(new ArrayList<>());
        else return new JsonArrayContainer<>(new ScorecardResourceAssembler()
                .toResources(Collections.singletonList(game.getScorecard())));
    }

    @Transactional
    @RequestMapping(value = "/{game:[0-9]+}/scorecards", method = RequestMethod.POST)
    public ScorecardResource createScorecard(@PathVariable Game game,
                                             @Validated(Scorecard.Creating.class)
                                             @RequestBody Scorecard scorecard,
                                             HttpServletResponse response) {
        if (game == null)
            throw new ResourceNotFoundException("game does not exist");
        if (game.getScorecard() != null)
            throw new GameAlreadyHasScorecardException();
        scorecard.setGame(game);

        scorecard.getSections().forEach(scorecardSection -> scorecardSection.setScorecard(scorecard));

        scorecard.getRobotRoles().forEach(robotRole -> robotRole.setScorecard(scorecard));
        scorecard.getRobotRoles().forEach(robotRole -> {
            robotRole.getWeights().forEach(weight -> weight.setRobotRole(robotRole));
        });
        scorecard.getRobotRoles().stream().flatMap(robotRole -> robotRole.getWeights().stream()).forEach(weight -> {
            Optional<FieldSection> section =
                    scorecard.getSections().stream()
                             .filter(scorecardSection -> scorecardSection.getIndex() == weight
                                     .getField().getIndex())
                             .filter(scorecardSection -> scorecardSection instanceof
                                     FieldSection)
                             .map(scorecardSection -> (FieldSection) scorecardSection)
                             .findFirst();
            if(!section.isPresent()) throw new IndexIsNotAFieldException();
            weight.setField(section.get());
            section.get().getWeights().add(weight);
        });

        Optional<RobotRole> defaultRole = scorecard.getRoles().stream().filter(role-> Objects
                .equals(role.getName(), scorecard.getDefaultRole().getName())).findFirst();

        if(!defaultRole.isPresent()) throw new DefaultRoleNotInRoleList();

        scorecard.setDefaultRole(defaultRole.get());

        ScorecardResource scorecardResource = new ScorecardResourceAssembler().toResource(
                scorecardRepository.saveAndFlush(scorecard));
        response.setHeader(LOCATION, scorecardResource.getLink("self").getHref());
        return scorecardResource;
    }

    @RequestMapping(value = "/{game:[0-9]+}/robots", method = RequestMethod.GET)
    public JsonArrayContainer<RobotResource> getAllRobots(@PathVariable Game game) {
        if (game == null)
            throw new ResourceNotFoundException("game does not exist");
        return new JsonArrayContainer<>(new RobotResourceAssembler().toResources(game.getRobots()));
    }

    @RequestMapping(value = "/{game:[0-9]+}/results", method = RequestMethod.GET)
    public JsonArrayContainer<ResultResource> getResults(@PathVariable Game game,
                                                         @RequestParam("scorecard") Scorecard scorecard){
        BiFunction<Game, Set<Result>, Set<Result>>
                averageResultsForGameFromAverageResultsForMatch =
                (game1, results) -> game1.getEvents().stream().flatMap(event->event.getMatches().stream())
                                          .flatMap(match -> match.getRobots().stream())
                                          .distinct()
                                          .map(robot -> {
                                              Set<Result> res = resultsForRobot.apply(results, robot);
                                              Result result =
                                                      resultsForRobot.apply(results, robot)
                                                                     .stream()
                                                                     .collect(ResultUtil.resultAverager());
                                              result.setRobot(robot);
                                              return result;
                                          })
                                          .collect(Collectors.toSet());

        Set<Result> res = game.getEvents().stream().flatMap(event -> event.getMatches().stream())
                              .map(match->averageResultsForMatch.apply(match,scorecard))
                              .flatMap(Collection::stream)
                              .collect(Collectors.toSet());

        Set<Result> results = averageResultsForGameFromAverageResultsForMatch.apply(
                game,
                game.getEvents().stream().flatMap(event -> event.getMatches().stream())
                     .map(match->averageResultsForMatch.apply(match,scorecard))
                     .flatMap(Collection::stream)
                     .collect(Collectors.toSet()));

        return new JsonArrayContainer<>(new ResultResourceAssembler().toResources(results));
    }

    @ResponseStatus(value = HttpStatus.UNPROCESSABLE_ENTITY,
            reason = "District does not exist")
    class DistrictDoesNotExistException extends RuntimeException {
    }

    @ResponseStatus(value = HttpStatus.CONFLICT,
            reason = "game already has a scorecard. currently, only one " +
                    "scorecard per game is supported")
    class GameAlreadyHasScorecardException extends RuntimeException {
    }

    @ResponseStatus(value = HttpStatus.UNPROCESSABLE_ENTITY,
    reason = "The index specified for the field for a weight does not exist or is not a field")
    class IndexIsNotAFieldException extends RuntimeException {
    }

    @ResponseStatus(value = HttpStatus.UNPROCESSABLE_ENTITY,
            reason = "The default role specified is not a role in the roles list")
    class DefaultRoleNotInRoleList extends RuntimeException{

    }
}
