package com.robocubs4205.cubscout.rest.v1;

import com.robocubs4205.cubscout.model.scorecard.Scorecard;
import com.robocubs4205.cubscout.model.scorecard.ScorecardRepository;
import com.robocubs4205.cubscout.rest.JsonArrayContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/scorecards",produces = "application/vnd.robocubs-v1+json")
public class ScorecardController {

    private final ScorecardRepository scorecardRepository;

    @Autowired
    public ScorecardController(ScorecardRepository scorecardRepository){

        this.scorecardRepository = scorecardRepository;
    }

    @RequestMapping(method = RequestMethod.GET)
    public JsonArrayContainer<ScorecardResource> getAllScorecards(){
        return new JsonArrayContainer<>(new ScorecardResourceAssembler().toResources(scorecardRepository.findAll()));
    }

    @RequestMapping(value = "/{scorecard:[0-9]+}",method=RequestMethod.GET)
    public ScorecardResource getScorecard(@PathVariable Scorecard scorecard){
        if(scorecard==null)throw new ResourceNotFoundException("scorecard does not exist");
        return new ScorecardResourceAssembler().toResource(scorecard);
    }

    @RequestMapping(value = "/{scorecard:[0-9]+}/results",method=RequestMethod.GET)
    public JsonArrayContainer<ResultResource> getResults(@PathVariable Scorecard scorecard) {
        if(scorecard==null)throw new ResourceNotFoundException("scorecard does not exist");
        return new JsonArrayContainer<>(new ResultResourceAssembler().toResources(scorecard.getResults()));
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @RequestMapping(value = "/{scorecard:[0-9]+}",method=RequestMethod.DELETE)
    public void deleteScorecard(@PathVariable Scorecard scorecard){
        if(scorecard==null)throw new ResourceNotFoundException("scorecard does not exist");
        scorecardRepository.delete(scorecard);
        scorecardRepository.flush();
    }
}
