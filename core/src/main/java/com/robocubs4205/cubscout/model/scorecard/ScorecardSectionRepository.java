package com.robocubs4205.cubscout.model.scorecard;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScorecardSectionRepository extends JpaRepository<ScorecardSection,Long>{
}
