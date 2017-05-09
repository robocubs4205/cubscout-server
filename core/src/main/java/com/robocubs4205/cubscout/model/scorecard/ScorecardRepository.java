package com.robocubs4205.cubscout.model.scorecard;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScorecardRepository extends JpaRepository<Scorecard,Long>{
    Scorecard findById(long id);
}
