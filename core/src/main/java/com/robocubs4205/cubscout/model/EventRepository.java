package com.robocubs4205.cubscout.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    Event findById(long id);

    Event findByShortName(String shortName);

    List<Event> findByGame(Game game);
}
