package com.robocubs4205.cubscout.rest.v1;

import com.robocubs4205.cubscout.rest.JsonArrayContainer;
import com.robocubs4205.cubscout.model.*;
import com.robocubs4205.cubscout.model.scorecard.Result;
import com.robocubs4205.cubscout.model.scorecard.Scorecard;
import com.robocubs4205.cubscout.util.ResultUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.robocubs4205.cubscout.util.ResultUtil.averageResultsForMatch;
import static com.robocubs4205.cubscout.util.ResultUtil.resultsForRobot;
import static com.robocubs4205.cubscout.util.ResultUtil.resultsForScorecard;
import static org.springframework.http.HttpHeaders.LOCATION;


@RestController
@RequestMapping(value = "/events", produces = "application/vnd.robocubs-v1+json")
public class EventController {
    private final EventRepository eventRepository;
    private final MatchRepository matchRepository;

    @Autowired
    public EventController(EventRepository eventRepository, MatchRepository matchRepository) {
        this.eventRepository = eventRepository;
        this.matchRepository = matchRepository;
    }

    @RequestMapping(method = RequestMethod.GET)
    JsonArrayContainer<EventResource> getAllEvents() {
        return new JsonArrayContainer<>(new EventResourceAssembler().toResources(eventRepository.findAll()));
    }

    @RequestMapping(value = "/{event:[0-9]+}", method = RequestMethod.GET)
    EventResource getEvent(@PathVariable Event event) {
        if (event == null) throw new ResourceNotFoundException("event does not exist");
        return new EventResourceAssembler().toResource(event);
    }


    @RequestMapping(value = "/{event:[0-9]+}", method = RequestMethod.PUT)
    EventResource updateEvent(@PathVariable Event event, @RequestBody Event newEvent) {
        if (event == null) throw new ResourceNotFoundException("event does not exist");
        event.setShortName(newEvent.getShortName());
        event.setDistrict(newEvent.getDistrict());
        event.setStartDate(newEvent.getStartDate());
        event.setEndDate(newEvent.getEndDate());
        event.setAddress(newEvent.getAddress());
        event = eventRepository.saveAndFlush(event);
        return new EventResourceAssembler().toResource(event);
    }

    @RequestMapping(value = "/{event:[0-9]+}", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteEvent(@PathVariable Event event) {
        if (event == null) throw new ResourceNotFoundException("event does not exist");
        eventRepository.delete(event);
        eventRepository.flush();
    }

    @RequestMapping(value = "/{event:[0-9]+}/matches", method = RequestMethod.GET)
    JsonArrayContainer<MatchResource> getAllMatches(@PathVariable Event event) {
        if (event == null) throw new ResourceNotFoundException("event does not exist");
        return new JsonArrayContainer<>(new MatchResourceAssembler().toResources(matchRepository.findByEvent(event)));
    }

    @RequestMapping(value = "/{event:[0-9]+}/matches", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED)
    MatchResource createMatch(@PathVariable Event event, @RequestBody Match match, HttpServletResponse response) {
        if (event == null) throw new ResourceNotFoundException("event does not exist");
        match.setEvent(event);
        match = matchRepository.saveAndFlush(match);
        MatchResource matchResource = new MatchResourceAssembler().toResource(match);
        response.setHeader(LOCATION,matchResource.getLink("self").getHref());
        return matchResource;
    }

    @RequestMapping(value = "/{event:[0-9]+}/results", method = RequestMethod.GET)
    JsonArrayContainer<ResultResource> getResults(@PathVariable Event event,
                                    @RequestParam("scorecard") Scorecard scorecard) {

        BiFunction<Event, Set<Result>, Set<Result>>
                averageResultsForEventFromAverageResultsForMatch =
                (event1, results) -> event.getMatches().stream()
                                          .flatMap(match -> match.getRobots().stream())
                                          .distinct()
                                          .map(robot -> {
                                              Result result =
                                                      resultsForRobot.apply(results, robot)
                                                                     .stream()
                                                                     .collect(ResultUtil.resultAverager());
                                              result.setRobot(robot);
                                              return result;
                                          })
                                          .collect(Collectors.toSet());

        Set<Result> results = averageResultsForEventFromAverageResultsForMatch.apply(
                event,
                event.getMatches().stream()
                     .map(match->averageResultsForMatch.apply(match,scorecard))
                     .flatMap(Collection::stream)
                     .collect(Collectors.toSet()));

        results.forEach(result->result.setScorecard(scorecard));

        return new JsonArrayContainer<>(new ResultResourceAssembler().toResources(results));
    }
}
