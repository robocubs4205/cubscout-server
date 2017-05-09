package com.robocubs4205.cubscout.rest.v1;

import org.springframework.hateoas.ResourceSupport;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

@RestController
@RequestMapping(value="/",produces = "application/vnd.robocubs-v1+json")
public class RootController {
    @RequestMapping(method = RequestMethod.GET)
    public ResourceSupport get(){
        ResourceSupport resource = new ResourceSupport();

        resource.add(linkTo(GameController.class).withRel("games"));
        resource.add(linkTo(EventController.class).withRel("events"));
        resource.add(linkTo(ScorecardController.class).withRel("scorecards"));
        resource.add(linkTo(TeamController.class).withRel("teams"));
        resource.add(linkTo(RobotController.class).withRel("robots"));
        resource.add(linkTo(DistrictController.class).withRel("districts"));
        resource.add(linkTo(MatchController.class).withRel("matches"));

        return resource;
    }
}
