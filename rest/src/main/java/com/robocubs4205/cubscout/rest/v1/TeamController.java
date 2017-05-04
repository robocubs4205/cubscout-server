package com.robocubs4205.cubscout.rest.v1;

import com.robocubs4205.cubscout.model.RobotRepository;
import com.robocubs4205.cubscout.model.Team;
import com.robocubs4205.cubscout.model.TeamRepository;
import com.robocubs4205.cubscout.rest.JsonArrayContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import static org.springframework.http.HttpHeaders.LOCATION;

@RestController
@PreAuthorize("denyAll()")
@RequestMapping(value = "/teams",produces = "application/vnd.robocubs-v1+json")
public class TeamController {
    private final TeamRepository teamRepository;
    private final RobotRepository robotRepository;

    @Autowired
    public TeamController(TeamRepository teamRepository, RobotRepository robotRepository) {
        this.teamRepository = teamRepository;
        this.robotRepository = robotRepository;
    }
    @RequestMapping(method = RequestMethod.GET)
    @PreAuthorize("permitAll()")
    JsonArrayContainer<TeamResource> getAll(){
        return new JsonArrayContainer<>(new TeamResourceAssembler().toResources(teamRepository.findAll()));
    }
    @RequestMapping(value = "/{team:[0-9]+}",method = RequestMethod.GET)
    @PreAuthorize("permitAll()")
    TeamResource get(@PathVariable Team team){
        if(team==null) throw new ResourceNotFoundException();
        return new TeamResourceAssembler().toResource(team);
    }
    @RequestMapping(method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('MANAGE_TEAMS','SCOUT_MATCHES')")
    public TeamResource create(@Valid @RequestBody Team team,HttpServletResponse response){
        team = teamRepository.save(team);
        TeamResource teamResource = new TeamResourceAssembler().toResource(team);
        response.setHeader(LOCATION,teamResource.getLink("self").getHref());
        return teamResource;
    }
    @RequestMapping(value = "/{team:[0-9]+}",method = RequestMethod.PUT)
    @PreAuthorize("hasRole('MANAGE_TEAMS')")
    public TeamResource update(@PathVariable Team team, @RequestBody Team newTeam){
        if(team==null) throw new ResourceNotFoundException();
        team.setNumber(newTeam.getNumber());
        team.setName(newTeam.getName());
        team = teamRepository.save(team);
        return new TeamResourceAssembler().toResource(team);
    }
    @RequestMapping(value = "/{team:[0-9]+}/matches",method = RequestMethod.GET)
    @PreAuthorize("permitAll()")
    public JsonArrayContainer<RobotResource> getRobots(@PathVariable Team team){
        return new JsonArrayContainer<>(new RobotResourceAssembler().toResources(robotRepository.find(team)));
    }
}
