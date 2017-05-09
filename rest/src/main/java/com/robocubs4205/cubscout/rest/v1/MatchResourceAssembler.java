package com.robocubs4205.cubscout.rest.v1;

import com.robocubs4205.cubscout.model.Match;
import org.springframework.hateoas.mvc.IdentifiableResourceAssemblerSupport;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

public class MatchResourceAssembler extends IdentifiableResourceAssemblerSupport<Match, MatchResource> {

    public MatchResourceAssembler() {
        super(MatchController.class, MatchResource.class);
    }

    @Override
    public MatchResource toResource(Match entity) {
        MatchResource resource = createResource(entity);
        resource.setMatchId(entity.getId());
        resource.setNumber(entity.getNumber());
        resource.setType(entity.getType());
        resource.add(linkTo(EventController.class).slash(entity.getEvent()).withRel("event"));
        resource.add(linkTo(MatchController.class).slash(entity).slash("results")
                                                  .withRel("results"));
        resource.add(linkTo(MatchController.class).slash(entity).slash("robots")
                                                  .withRel("robots"));
        return resource;
    }
}
