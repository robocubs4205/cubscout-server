package com.robocubs4205.cubscout.rest.v1;

import com.robocubs4205.cubscout.model.Event;
import org.springframework.hateoas.mvc.IdentifiableResourceAssemblerSupport;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

/**
 * Created by trevor on 2/14/17.
 */
public class EventResourceAssembler extends IdentifiableResourceAssemblerSupport<Event, EventResource> {
    public EventResourceAssembler() {
        super(EventController.class, EventResource.class);
    }

    @Override
    public EventResource toResource(Event entity) {
        EventResource resource = createResource(entity, entity.getGame());
        resource.setShortName(entity.getShortName());
        resource.setEventId(entity.getId());
        resource.setAddress(entity.getAddress());
        resource.setStartDate(entity.getStartDate());
        resource.setEndDate(entity.getEndDate());
        if (entity.getDistrict() != null) {
            resource.add(linkTo(DistrictController.class).slash(entity.getDistrict())
                                                         .withRel("district"));
        }
        resource.add(linkTo(GameController.class).slash(entity.getGame()).withRel("game"));
        resource.add(linkTo(EventController.class).slash(entity).slash("matches")
                                                  .withRel("matches"));
        resource.add(linkTo(EventController.class).slash(entity).slash("results").withRel("results"));
        return resource;
    }
}
