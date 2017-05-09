package com.robocubs4205.cubscout.tbascraper;

import com.robocubs4205.cubscout.model.*;
import com.robocubs4205.cubscout.tbascraper.model.TBAEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

@Component
public final class TBAScraper {

    private final RestTemplate restTemplate;
    private final GameRepository gameRepository;
    private final DistrictRepository districtRepository;
    private final EventRepository eventRepository;

    private final String getEventsForYearString = "https://www.thebluealliance.com/api/v2/events/{year}";

    @Autowired
    public TBAScraper(RestTemplate restTemplate, GameRepository gameRepository, DistrictRepository districtRepository,
                      EventRepository eventRepository) {
        this.restTemplate = restTemplate;
        this.gameRepository = gameRepository;
        this.districtRepository = districtRepository;
        this.eventRepository = eventRepository;
    }

    @Bean
    public static RestTemplate restTemplate(){
        return new RestTemplate();
    }

    @Scheduled(cron = "0 0 12 1 * ?")
    public void getEvents() {
        String AppVersion = getClass().getPackage().getImplementationVersion();
        if (AppVersion == null) {
            //The Blue Alliance requires an application version to be included when using their api
            System.out.println("failed to get implemented version for X-TBA-App-Id header");
            return;
        }
        gameRepository.findAll().forEach(game -> {
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-TBA-App-Id", "4205:scouting app:" + this.getClass().getPackage().getImplementationVersion());
            headers.set("Accept","application/json");
            headers.set("User-Agent","cubscout");
            HttpEntity<?> requestEntity = new HttpEntity<>(headers);
            ResponseEntity<TBAEvent[]> response =
                    restTemplate.exchange(getEventsForYearString, HttpMethod.GET, requestEntity, TBAEvent[].class,game.getYear());
            Arrays.asList(response.getBody()).forEach(tbaEvent->{
                //todo: better checking for existing event
                if(eventRepository.findByShortName(tbaEvent.getShortName())!=null) return;
                District district = null;
                String districtCode = districtCodeFromTBADistrictNumber(tbaEvent.getDistrict());
                if(districtCode!=null){
                    district = districtRepository.findByCode(districtCode);
                    if(district==null){
                        district = new District(districtCode);
                        districtRepository.saveAndFlush(district);
                    }
                }
                Event event = tbaEvent.toEvent(game,district);
                eventRepository.saveAndFlush(event);
            });
        });
    }

    private String districtCodeFromTBADistrictNumber(int number) {
        switch (number) {
            case 1:
                return "FIM";
            case 2:
                return "MAR";
            case 3:
                return "NE";
            case 4:
                return "PNW";
            case 5:
                return "IN";
            case 6:
                return "CHS";
            case 7:
                return "NC";
            case 8:
                return "PCH";
            case 9:
                return "ONT";
            case 10:
                return "isr";
            default:
                return null;
        }
    }

    private class VersionNotFoundException extends Exception {
    }
}
