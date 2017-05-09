package com.robocubs4205.cubscout.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface TeamRepository extends JpaRepository<Team,Long>{
    Team findById(int id);
    Set<Team> findByNumber(int number);

    Team findByNumberAndGameType(int number, String gameType);
}
