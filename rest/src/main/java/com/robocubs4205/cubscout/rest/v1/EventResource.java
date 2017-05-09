package com.robocubs4205.cubscout.rest.v1;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.hateoas.ResourceSupport;

import java.util.Date;

/**
 * Created by trevor on 2/14/17.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EventResource extends ResourceSupport {
    private long id;
    private String shortName;
    private String address;
    private Date startDate;
    private Date endDate;

    public EventResource(){}

    @JsonProperty("id")
    public long getEventId() {
        return id;
    }

    public void setEventId(long id) {
        this.id = id;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }
}
