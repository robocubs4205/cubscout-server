package com.robocubs4205.cubscout.rest.v1;

import com.robocubs4205.cubscout.model.Robot;
import org.springframework.hateoas.mvc.IdentifiableResourceAssemblerSupport;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

/**
 * Created by trevor on 2/18/17.
 */
public class RobotResourceAssembler extends IdentifiableResourceAssemblerSupport<Robot, RobotResource> {
    public RobotResourceAssembler() {
        super(RobotController.class, RobotResource.class);
    }

    @Override
    public RobotResource toResource(Robot entity) {
        RobotResource resource = createResource(entity);
        resource.setRobotId(entity.getId());
        resource.setNumber(entity.getNumber());
        resource.setYear(entity.getYear());
        resource.setName(entity.getName());
        resource.add(linkTo(TeamController.class).slash(entity.getTeam()).withRel("team"));
        resource.add(linkTo(RobotController.class).slash(entity).slash("matches")
                                                  .withRel("matches"));
        resource.add(linkTo(GameController.class).slash(entity.getGame()).withRel("game"));
        return resource;
    }
}
