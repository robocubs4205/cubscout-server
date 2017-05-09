package com.robocubs4205.cubscout.model.scorecard;

import com.robocubs4205.cubscout.model.Match;
import com.robocubs4205.cubscout.model.Robot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResultRepository extends JpaRepository<Result,Long>{
    List<Result> findByRobot(Robot robot);
    List<Result> findByMatch(Match match);
    Result findByRobotAndMatch(Robot robot, Match match);
}
