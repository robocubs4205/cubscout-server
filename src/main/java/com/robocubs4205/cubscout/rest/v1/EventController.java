package com.robocubs4205.cubscout.rest.v1;

import com.robocubs4205.cubscout.model.Event;
import com.robocubs4205.cubscout.model.EventRepository;
import com.robocubs4205.cubscout.model.Match;
import com.robocubs4205.cubscout.model.MatchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

import static org.springframework.http.HttpHeaders.LOCATION;


@RestController
@RequestMapping(value = "/events",produces = "application/vnd.robocubs-v1+json")
public class EventController {
    private final EventRepository eventRepository;
    private final MatchRepository matchRepository;

    @Autowired
    public EventController(EventRepository eventRepository, MatchRepository matchRepository) {
        this.eventRepository = eventRepository;
        this.matchRepository = matchRepository;
    }

    @RequestMapping(method = RequestMethod.GET)
    List<EventResource> getAllEvents() {
        return new EventResourceAssembler().toResources(eventRepository.findAll());
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
        eventRepository.save(event);
        return new EventResourceAssembler().toResource(event);
    }

    @RequestMapping(value = "/{event:[0-9]+}", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteEvent(@PathVariable Event event) {
        if (event == null) throw new ResourceNotFoundException("event does not exist");
        eventRepository.delete(event);
    }

    @RequestMapping(value = "/{event:[0-9]+}/matches", method = RequestMethod.GET)
    List<MatchResource> getAllMatches(@PathVariable Event event) {
        if (event == null) throw new ResourceNotFoundException("event does not exist");
        return new MatchResourceAssembler().toResources(matchRepository.findByEvent(event));
    }

    @RequestMapping(value = "/{event:[0-9]+}/matches", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED)
    MatchResource createMatch(@PathVariable Event event, @RequestBody Match match, HttpServletResponse response) {
        if (event == null) throw new ResourceNotFoundException("event does not exist");
        match.setEvent(event);
        matchRepository.save(match);
        MatchResource matchResource = new MatchResourceAssembler().toResource(match);
        response.setHeader(LOCATION,matchResource.getLink("self").getHref());
        return matchResource;
    }
}
