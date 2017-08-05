package com.robocubs4205.cubscout

import java.sql.Date
import java.time.temporal.ChronoField
import java.time.{LocalDate, Year}
import javax.inject.{Inject, Named}

import com.robocubs4205.cubscout.model._
import com.robocubs4205.cubscout.model.scorecard.Result
import play.api.Environment
import play.api.Application
import play.api.Mode.Test
import play.api.db.slick.{DatabaseConfigProvider, DbName, DefaultSlickApi, SlickApi}
import slick.jdbc.JdbcProfile
import slick.model.ForeignKeyAction.Restrict

import scala.concurrent.ExecutionContext


/**
  * Created by trevor on 7/20/17.
  */
class CubScoutDb @Inject()(dbConfigProvider: DatabaseConfigProvider)(implicit ex: ExecutionContext) {
  val dbConfig = dbConfigProvider.get[JdbcProfile]


  import dbConfig._
  import profile.api._

  implicit val localDateToSqlDate = MappedColumnType.base[LocalDate, Date](
    Date.valueOf,
    _.toLocalDate
  )

  implicit val yearToLong = MappedColumnType.base[Year, Int](
    _.get(ChronoField.YEAR),
    Year.of
  )

  class DistrictTable(tag: Tag) extends Table[District](tag, "districts") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def code = column[String]("code", O.Unique)

    def name = column[String]("name", O.Unique)

    def gameType = column[String]("gameType")

    def firstYear = column[Year]("firstYear")

    def lastYear = column[Option[Year]]("lastYear")

    def * = (id, code, name, gameType, firstYear, lastYear) <> (District.tupled, District.unapply)
  }

  class EventTable(tag: Tag) extends Table[Event](tag, "events") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def name = column[String]("name")

    def address = column[Option[String]]("address")

    def startDate = column[Option[LocalDate]]("startDate")

    def endDate = column[Option[LocalDate]]("endDate")

    def districtId = column[Option[Long]]("districtId")

    def gameId = column[Long]("gameId")

    def districtFk = foreignKey("event_district_fk", districtId, districts)(_.id.?, onUpdate = Restrict, onDelete = Restrict)

    def gameFk = foreignKey("event_game_fk", gameId, games)(_.id, onUpdate = Restrict, onDelete = Restrict)

    def uniqueGameAndName = index("game_name_ux", (gameId, name), unique = true)

    def * = (id, districtId, gameId, name, address, startDate, endDate) <> (Event.tupled, Event.unapply)
  }

  class GameTable(tag: Tag) extends Table[Game](tag, "games") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def name = column[String]("name", O.Unique)

    def `type` = column[String]("type")

    def year = column[Year]("year")

    def * = (id, name, `type`, year) <> (Game.tupled, Game.unapply)
  }

  class MatchTable(tag: Tag) extends Table[Match](tag, "matches") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def eventId = column[Long]("eventId")

    def eventFk = foreignKey("match_event_fk", eventId, events)(_.id, onUpdate = Restrict, onDelete = Restrict)

    def number = column[Long]("number")

    def `type` = column[String]("type")

    def * = (id, eventId, number, `type`) <> (Match.tupled, Match.unapply)
  }

  class TeamTable(tag: Tag) extends Table[Team](tag, "teams") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def number = column[Long]("number")

    def name = column[Option[String]]("name")

    def gameType = column[String]("gameType")

    def districtId = column[Option[Long]]("districtId")

    def districtFk = foreignKey("team_district_fk", districtId, districts)(_.id.?, onUpdate = Restrict, onDelete = Restrict)

    def * = (id, number, name, gameType, districtId) <> (Team.tupled, Team.unapply)
  }

  class RobotTable(tag: Tag) extends Table[Robot](tag, "robots") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def teamId = column[Long]("teamId")

    def teamFk = foreignKey("robot_team_fk", teamId, teams)(_.id, onUpdate = Restrict, onDelete = Restrict)

    def gameId = column[Long]("gameId")

    def gameFk = foreignKey("robot_game_fk", gameId, games)(_.id, onUpdate = Restrict, onDelete = Restrict)

    def name = column[Option[String]]("name")

    def * = (id, teamId, gameId, name) <> (Robot.tupled, Robot.unapply)
  }

  class ResultTable(tag: Tag) extends Table[Result](tag, "teamsInMatches") {
    def id = column[Long]("id", O.AutoInc, O.PrimaryKey)

    def teamId = column[Long]("teamId")

    def teamFk = foreignKey("result_team_fk", teamId, teams)(_.id, onUpdate = Restrict, onDelete = Restrict)

    def matchId = column[Long]("matchId")

    def matchFk = foreignKey("result_match_fk", matchId, matches)(_.id, onUpdate = Restrict, onDelete = Restrict)

    def scorecardId = column[Long]("scorecardId")

    def pk = primaryKey("result_pk", (teamId, matchId))

    def * = (id, teamId, matchId, scorecardId) <> (Result.tupled, Result.unapply)
  }

  val districts = TableQuery[DistrictTable]
  val events = TableQuery[EventTable]
  val games = TableQuery[GameTable]
  val matches = TableQuery[MatchTable]
  val teams = TableQuery[TeamTable]
  val robots = TableQuery[RobotTable]
  val results = TableQuery[ResultTable]
}
