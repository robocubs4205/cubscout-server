package com.robocubs4205.cubscout.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by trevor on 2/15/17.
 */
@Transactional
@Repository
public interface GameRepository extends JpaRepository<Game,Long>{
    Game findById(int id);

    Game findByName(String name);

    List<Game> findByYear(int year);
}
