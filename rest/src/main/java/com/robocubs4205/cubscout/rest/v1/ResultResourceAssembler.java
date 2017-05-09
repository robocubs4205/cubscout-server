package com.robocubs4205.cubscout.rest.v1;

import com.robocubs4205.cubscout.model.scorecard.Result;
import org.springframework.hateoas.mvc.IdentifiableResourceAssemblerSupport;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

public class ResultResourceAssembler extends IdentifiableResourceAssemblerSupport<Result, ResultResource> {
    ResultResourceAssembler() {
        super(RobotController.class, ResultResource.class);
    }

    @Override
    public ResultResource toResource(Result entity) {
        ResultResource resultResource = new ResultResource();
        resultResource.setResultId(entity.getId());
        resultResource.setScores(entity.getScores());
        resultResource.add(linkTo(ResultController.class).slash(entity)
                                                        .withSelfRel());
        resultResource.add(linkTo(MatchController.class).slash(entity.getMatch())
                                                        .withRel("match"));
        resultResource.add(linkTo(RobotController.class).slash(entity.getRobot())
                                                        .withRel("robot"));
        resultResource.add(linkTo(ScorecardController.class).slash(entity.getScorecard())
                                                            .withRel("scorecard"));
        return resultResource;
    }
}
