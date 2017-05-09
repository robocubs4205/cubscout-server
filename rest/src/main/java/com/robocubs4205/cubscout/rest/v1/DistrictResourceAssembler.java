package com.robocubs4205.cubscout.rest.v1;

import com.robocubs4205.cubscout.model.District;
import org.springframework.hateoas.mvc.IdentifiableResourceAssemblerSupport;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

/**
 * Created by trevor on 2/15/17.
 */
public class DistrictResourceAssembler extends
        IdentifiableResourceAssemblerSupport<District, DistrictResource> {
    public DistrictResourceAssembler() {
        super(DistrictController.class, DistrictResource.class);
    }

    @Override
    public DistrictResource toResource(District entity) {
        DistrictResource resource = createResource(entity);
        resource.add(linkTo(DistrictController.class).slash(entity).withSelfRel());
        resource.setCode(entity.getCode());
        resource.setName(entity.getName());
        resource.add(linkTo(DistrictController.class).slash(entity).slash("events")
                .withRel("events"));
        resource.add(linkTo(DistrictController.class).slash(entity).slash("teams")
                .withRel("teams"));
        return resource;
    }
}
