package com.robocubs4205.cubscout.rest.v1;

import com.robocubs4205.cubscout.model.scorecard.Scorecard;
import org.springframework.hateoas.mvc.IdentifiableResourceAssemblerSupport;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

public class ScorecardResourceAssembler extends
        IdentifiableResourceAssemblerSupport<Scorecard, ScorecardResource> {
    public ScorecardResourceAssembler() {
        super(ScorecardController.class, ScorecardResource.class);
    }

    @Override
    public ScorecardResource toResource(Scorecard entity) {
        ScorecardResource resource = createResource(entity);
        resource.setScorecardId(entity.getId());
        resource.setSections(entity.getSections());
        resource.setRoles(entity.getRoles());
        resource.setDefaultRole(entity.getDefaultRole());
        resource.add(linkTo(GameController.class).slash(entity.getGame())
                                                 .withRel("game"));
        resource.add(linkTo(ScorecardController.class).slash(entity).slash("results")
                                                      .withRel("results"));
        return resource;
    }
}
