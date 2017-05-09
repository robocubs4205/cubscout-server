package com.robocubs4205.cubscout.tbascraper.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.robocubs4205.cubscout.model.District;
import com.robocubs4205.cubscout.model.Event;
import com.robocubs4205.cubscout.model.Game;

/**
 * Created by trevor on 3/20/17.
 */
public class TBAEvent {
    @JsonProperty
    private int year;
    @JsonProperty
    private String short_name;
    @JsonProperty
    private int district;
    @JsonProperty
    private String venue_address;

    public Event toEvent(Game game, District district){
        Event event = new Event();
        event.setGame(game);
        event.setDistrict(district);
        event.setAddress(venue_address);
        event.setShortName(short_name);
        return event;
    }

    public String getShortName() {
        return short_name;
    }

    public void setShortName(String short_name) {
        this.short_name = short_name;
    }

    public String getVenueAddress() {
        return venue_address;
    }

    public void setVenueAddress(String venue_address) {
        this.venue_address = venue_address;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getDistrict() {
        return district;
    }

    public void setDistrict(int district) {
        this.district = district;
    }
}
