package com.robocubs4205.cubscout.api.v1.services

import javax.inject.Inject

import com.robocubs4205.cubscout.CubScoutDb
import com.robocubs4205.cubscout.api.v1.controllers.{GameNotFoundException, TeamNotFoundException}
import com.robocubs4205.cubscout.model.Robot

import scala.concurrent.ExecutionContext

/**
  * Created by trevor on 8/5/17.
  */
class RobotService @Inject()(csdb: CubScoutDb)(implicit ec: ExecutionContext) {

  import csdb._
  import dbConfig._
  import profile.api._

  def findById(id:Long) = robots.filter(_.id===id).result.headOption

  def checkNoInsertConflict(robot:Robot) = {
    for{
      team <- teams.filter(_.id===robot.teamId).result.headOption
      game <- games.filter(_.id===robot.gameId).result.headOption
      sameRobot <- robots.filter(_.teamId===robot.teamId).filter(_.gameId===robot.gameId).result.headOption
    } yield if(team.isEmpty) DBIO.failed(TeamNotFoundException())
    else if (game.isEmpty) DBIO.failed(GameNotFoundException())
    else if (sameRobot.isDefined) DBIO.failed(RobotAlreadyExistsException())
    else DBIO.successful(())
  }.flatten

  def checkNoReplaceConflict(robot:Robot,id:Long) = {
    for{
      team <- teams.filter(_.id===robot.teamId).result.headOption
      game <- games.filter(_.id===robot.gameId).result.headOption
      sameRobot <- robots.filter(_.teamId===robot.teamId).filter(_.gameId===robot.gameId).filter(_.id=!=id)
        .result.headOption
    } yield if(team.isEmpty) DBIO.failed(TeamNotFoundException())
    else if (game.isEmpty) DBIO.failed(GameNotFoundException())
    else if (sameRobot.isDefined) DBIO.failed(RobotAlreadyExistsException())
    else DBIO.successful(())
  }.flatten

  def checkNoDeleteConflict(id:Long) = {
    for{
      results <- results.filter(_.robotId===id).result
    } yield if(results.nonEmpty) DBIO.failed(ResultsForRobotException())
    else DBIO.successful(())
  }.flatten

  def doInsert(robot:Robot) =
    (robots returning robots.map(_.id) into ((robot,id)=>robot.copy(id=id)))+=robot

  def doReplace(robot:Robot,id:Long) =
    robots.filter(_.id===id).update(robot.copy(id=id)).map(_=>robot.copy(id=id))

  def doDelete(id:Long) = for{
    robot <- findById(id)
    _ <- robots.filter(_.id===id).delete
  } yield robot

  case class RobotAlreadyExistsException()
    extends RuntimeException("A robot with that team and game already exists")

  case class ResultsForRobotException()
    extends RuntimeException("There are results for that robot, which prevents its deletion")
}
