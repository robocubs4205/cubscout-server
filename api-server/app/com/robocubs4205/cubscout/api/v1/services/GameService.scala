package com.robocubs4205.cubscout.api.v1.services

import javax.inject.Inject

import com.robocubs4205.cubscout.CubScoutDb
import com.robocubs4205.cubscout.model.Game

import scala.concurrent.ExecutionContext

/**
  * Created by trevor on 8/4/17.
  */
class GameService @Inject()(csdb: CubScoutDb)(implicit ec: ExecutionContext) {

  import csdb._
  import dbConfig._
  import profile.api._

  def checkNoInsertConflict(game: Game) = {
    for {
      sameName <- games.filter(_.name === game.name).result.headOption
      sameYearAndType <- games.filter(_.year === game.year).filter(_.`type` === game.`type`).result.headOption
    } yield if (sameName.isDefined) DBIO.failed(GameWithSameNameExistsException())
    else if (sameYearAndType.isDefined) DBIO.failed(GameWithSameYearAndTypeException())
    else DBIO.successful(())
  }.flatten

  def checkNoReplaceConflict(game: Game, id: Long) = {
    for {
      sameName <- games.filter(_.name === game.name).filter(_.id =!= id).result.headOption
      sameYearAndType <- games.filter(_.year === game.year).filter(_.`type` === game.`type`).filter(_.id =!= id)
        .result.headOption
    } yield if (sameName.isDefined) DBIO.failed(GameWithSameNameExistsException())
    else if (sameYearAndType.isDefined) DBIO.failed(GameWithSameYearAndTypeException())
    else DBIO.successful(())
  }.flatten

  def checkNoDeleteConflict(id: Long) = {
    for (
      eventsInGame <- events.filter(event => event.gameId === id).result;
      robotsInGame <- robots.filter(robot => robot.gameId === id).result
    ) yield if (eventsInGame.nonEmpty) DBIO.failed(EventsInGameException())
    else if (robotsInGame.nonEmpty) DBIO.failed(RobotsInGameException())
    else DBIO.successful(())
  }.flatten

  def findById(id: Long) = games.filter(_.id === id).result.headOption

  def doReplace(game: Game, id: Long) = {
    games.filter(_.id === id).map(g => (g.name, g.`type`, g.year))
      .update(game.name, game.`type`, game.year)
  }.map(_ => Some(game.copy(id = id)))

  def doInsert(game: Game) = (games returning games.map(_.id) into ((game, id) => game.copy(id = id))) += game

  def doDelete(id: Long) = for {
    foundGame <- findById(id)
    _ <- games.filter(_.id === id).delete
  } yield foundGame

  case class EventsInGameException()
    extends RuntimeException("there are events for that game, which prevents its deletion")

  case class RobotsInGameException()
    extends RuntimeException("there are robots for that game, which prevents its deletion")

  case class GameWithSameNameExistsException() extends RuntimeException("A game with that name already exists")

  case class GameWithSameYearAndTypeException()
    extends RuntimeException("A game with that year and type already exists")

}
