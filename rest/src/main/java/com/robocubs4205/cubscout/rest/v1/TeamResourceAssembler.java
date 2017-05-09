package com.robocubs4205.cubscout.rest.v1;

import com.robocubs4205.cubscout.model.Team;
import org.springframework.hateoas.mvc.IdentifiableResourceAssemblerSupport;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

/**
 * Created by trevor on 2/18/17.
 */
public class TeamResourceAssembler extends
        IdentifiableResourceAssemblerSupport<Team, TeamResource> {
    public TeamResourceAssembler() {
        super(TeamController.class, TeamResource.class);
    }

    @Override
    public TeamResource toResource(Team entity) {
        TeamResource resource = createResource(entity);
        resource.setName(entity.getName());
        resource.setNumber(entity.getNumber());
        resource.setTeamId(entity.getId());
        resource.setGameType(entity.getGameType());
        resource.add(linkTo(TeamController.class).slash(entity).slash("robots")
                                                 .withRel("robots"));
        if (entity.getDistrict() != null) {
            resource.add(linkTo(DistrictController.class).slash(entity.getDistrict())
                    .withRel("district"));
        }
        return resource;
    }
}
