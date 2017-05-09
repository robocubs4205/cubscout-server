package com.robocubs4205.cubscout.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by trevor on 2/15/17.
 */
@Repository
@Transactional
public interface DistrictRepository extends JpaRepository<District,String> {
    District findByCode(String code);
}
