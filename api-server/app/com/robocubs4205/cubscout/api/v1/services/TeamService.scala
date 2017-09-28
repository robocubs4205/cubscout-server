package com.robocubs4205.cubscout.api.v1.services

import javax.inject.Inject

import com.robocubs4205.cubscout.CubScoutDb
import com.robocubs4205.cubscout.api.v1.controllers.DistrictNotFoundException
import com.robocubs4205.cubscout.model.Team

import scala.concurrent.ExecutionContext

/**
  * Created by trevor on 8/5/17.
  */
class TeamService @Inject()(csdb: CubScoutDb)(implicit ec: ExecutionContext) {

  import csdb._
  import dbConfig._
  import profile.api._

  def findById(id: Long) = teams.filter(_.id === id).result.headOption

  def checkNoInsertConflict(team: Team) = {
    for {
      sameTeam <- teams.filter(_.number === team.number).filter(_.gameType === team.gameType).result.headOption
      district <- districts.filter(_.id === team.districtId).result.headOption
    } yield if (sameTeam.isDefined) DBIO.failed(TeamAlreadyExistsException())
    else if (district.isEmpty && team.districtId.isDefined) DBIO.failed(DistrictNotFoundException())
    else DBIO.successful(())
  }.flatten

  def checkNoReplaceConflict(team: Team, id: Long) = {
    for {
      sameTeam <- teams.filter(_.number === team.number).filter(_.gameType === team.gameType).filter(_.id =!= id)
        .result.headOption
      district <- districts.filter(_.id === team.districtId).result.headOption
    } yield if (sameTeam.isDefined) DBIO.failed(TeamAlreadyExistsException())
    else if (district.isEmpty && team.districtId.isDefined) DBIO.failed(DistrictNotFoundException())
    else DBIO.successful(())
  }.flatten

  def checkNoDeleteConflict(id: Long) = {
    for {
      robots <- robots.filter(_.teamId === id).result.headOption
    } yield if (robots.nonEmpty) DBIO.failed(RobotsInTeamException())
    else DBIO.successful(())
  }.flatten

  def doInsert(team: Team) =
    (teams returning teams.map(t => t.id) into ((team, id) => team.copy(id = id))) += team

  def doReplace(team: Team, id: Long) =
    teams.filter(_.id === id).update(team.copy(id = id)).map(_ => team.copy(id = id))

  def doDelete(id: Long) = for {
    team <- findById(id)
    _ <- teams.filter(_.id === id).delete
  } yield team

  case class TeamAlreadyExistsException()
    extends RuntimeException("A team with that number and game type already exists")

  case class RobotsInTeamException()
    extends RuntimeException("There are robots in that team, which prevents its deletion")

}
