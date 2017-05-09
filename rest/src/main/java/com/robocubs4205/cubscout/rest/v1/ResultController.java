package com.robocubs4205.cubscout.rest.v1;

import com.robocubs4205.cubscout.model.scorecard.Result;
import com.robocubs4205.cubscout.model.scorecard.ResultRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/results")
public class ResultController {

    private final ResultRepository resultRepository;

    @Autowired
    public ResultController(ResultRepository resultRepository) {
        this.resultRepository = resultRepository;
    }

    @RequestMapping(value="/{result:[0-9]+}",method=RequestMethod.GET)
    public ResultResource getResult(@PathVariable Result result){
        if(result==null) throw new ResourceNotFoundException();
        return new ResultResourceAssembler().toResource(result);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @RequestMapping(value="/{result:[0-9]+}",method= RequestMethod.DELETE)
    public void deleteResult(@PathVariable Result result){
        if(result==null) throw new ResourceNotFoundException();
        this.resultRepository.delete(result);
    }
}
