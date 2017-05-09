package com.robocubs4205.cubscout.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Transactional
public interface MatchRepository extends JpaRepository<Match,Long>{
    Match findById(int id);

    List<Match> findByEvent(Event event);
}
