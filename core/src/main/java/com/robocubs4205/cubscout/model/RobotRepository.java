package com.robocubs4205.cubscout.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RobotRepository extends JpaRepository<Robot,Long>{
    Robot findById(long id);
    List<Robot> findByTeam(Team team);
    @Query("select r from Robot r where r.game = :game and r.number = :number")
    Robot findByNumberAndGame(@Param("number")int number,@Param("game") Game game);
}
