package com.robocubs4205.cubscout.rest.v1;

import com.robocubs4205.cubscout.model.Robot;
import com.robocubs4205.cubscout.model.RobotRepository;
import com.robocubs4205.cubscout.model.scorecard.ResultRepository;
import com.robocubs4205.cubscout.rest.JsonArrayContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

import static org.springframework.http.HttpHeaders.LOCATION;

@RestController
@RequestMapping(value = "/robots",produces = "application/vnd.robocubs-v1+json")
public class RobotController {
    private final RobotRepository robotRepository;
    private final ResultRepository resultRepository;

    @Autowired
    public RobotController(RobotRepository robotRepository, ResultRepository resultRepository) {
        this.robotRepository = robotRepository;
        this.resultRepository = resultRepository;
    }

    @RequestMapping(method = RequestMethod.GET)
    public JsonArrayContainer<RobotResource> getAll() {
        return new JsonArrayContainer<>(new RobotResourceAssembler().toResources(robotRepository.findAll()));
    }

    @RequestMapping(value = "/{robot:[0-9]+}", method = RequestMethod.GET)
    public RobotResource getRobot(@PathVariable Robot robot) {
        if (robot == null) throw new ResourceNotFoundException();
        return new RobotResourceAssembler().toResource(robot);
    }

    @RequestMapping(method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED)
    public RobotResource create(@RequestBody Robot robot, HttpServletResponse response) {
        robot = robotRepository.saveAndFlush(robot);
        RobotResource robotResource = new RobotResourceAssembler().toResource(robot);
        response.setHeader(LOCATION,robotResource.getLink("self").getHref());
        return robotResource;
    }

    @RequestMapping(value = "/{robot:[0-9]+}", method = RequestMethod.PUT)
    public RobotResource update(@PathVariable Robot robot, @RequestBody Robot newRobot) {
        robot.setYear(newRobot.getYear());
        robot = robotRepository.saveAndFlush(robot);
        return new RobotResourceAssembler().toResource(robot);
    }

    @RequestMapping(value = "/{robot:[0-9]+}", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Robot robot) {
        robotRepository.delete(robot);
        robotRepository.flush();
    }

    @RequestMapping(value = "/{robot:[0-9]+}/results",method = RequestMethod.GET)
    public JsonArrayContainer<ResultResource> getResults(@PathVariable Robot robot) {
        if (robot == null) throw new ResourceNotFoundException();
        return new JsonArrayContainer<>(new ResultResourceAssembler().toResources(resultRepository.findByRobot(robot)));
    }

    @RequestMapping(value = "/{robot:[0-9]+}/matches",method = RequestMethod.GET)
    public JsonArrayContainer<MatchResource> getMatches(@PathVariable Robot robot) {
        if (robot == null) throw new ResourceNotFoundException();
        return new JsonArrayContainer<>(new MatchResourceAssembler().toResources(robot.getMatches()));
    }
}
