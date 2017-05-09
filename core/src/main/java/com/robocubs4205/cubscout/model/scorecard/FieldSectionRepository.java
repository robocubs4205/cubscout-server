package com.robocubs4205.cubscout.model.scorecard;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FieldSectionRepository extends JpaRepository<FieldSection,Long>{
    FieldSection findById(long id);
    //@Query("select u from FieldSection u inner join u.scorecard s where u.id = :id and s.id = :scorecardId")
    //FieldSection findByIdAndScorecardId(@Param("id") long id, @Param("scorecardId") long scorecardId);
    FieldSection findByIdAndScorecard(long id, Scorecard scorecard);
}
