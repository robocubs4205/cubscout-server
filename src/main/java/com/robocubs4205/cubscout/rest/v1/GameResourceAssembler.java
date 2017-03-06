package com.robocubs4205.cubscout.rest.v1;

import com.robocubs4205.cubscout.model.Game;
import org.springframework.hateoas.mvc.IdentifiableResourceAssemblerSupport;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

/**
 * Created by trevor on 2/15/17.
 */
public class GameResourceAssembler extends IdentifiableResourceAssemblerSupport<Game, GameResource> {
    public GameResourceAssembler() {
        super(GameController.class, GameResource.class);
    }

    @Override
    public GameResource toResource(Game entity) {
        GameResource resource = createResource(entity);
        resource.setGameId(entity.getId());
        resource.setName(entity.getName());
        resource.setType(entity.getType());
        resource.setYear(entity.getYear());
        if (entity.getScorecard() != null) {
            resource.add(linkTo(ScorecardController.class).slash(entity.getScorecard()).withRel("scorecard"));
        }
        resource.add(linkTo(GameController.class).slash(entity).slash("events").withRel("events"));
        return resource;
    }
}
