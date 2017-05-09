package com.robocubs4205.cubscout.rest.v1;

import org.springframework.hateoas.ResourceSupport;

/**
 * Created by trevor on 2/15/17.
 */
public class DistrictResource extends ResourceSupport {
    private String code;
    private String name;
    public DistrictResource(){}

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
