package com.robocubs4205.cubscout

import java.time.{LocalDate, Year}

import com.robocubs4205.cubscout.model.{District, Event, Robot, Team}
import sangria.execution.deferred.{Deferred, DeferredResolver, UnsupportedDeferError}
import sangria.schema._
import slick.dbio.DBIO

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

package object graphql {
  val DateType = ScalarAlias(
    StringType,
    (date: LocalDate) => date.toString,
    (s: String) => Try(LocalDate.parse(s)).toEither match {
      case Left(_) => Left(DateCoercionViolation)
      case Right(v) => Right(v)
    }
  )

  val YearType = ScalarAlias(
    IntType,
    (year: Year) => year.toString,
    (i: Int) => Right(Year.of(i))
  )

  lazy val TeamType = ObjectType(
    name = "Team",
    description = "A robotics team",
    fields = fields[Unit, Team](
      Field("id", IDType, resolve = _ => ???),
      Field("name", OptionType(StringType), resolve = _ => ???),
      Field("number", IntType, resolve = _ => ???),
      Field("gameType", StringType, description = Some("Whether the team participates in FTC, FRC, etc."), resolve = _ => ???),
      Field("district", OptionType(DistrictType), resolve = _ => ???),
      Field("robots", ListType(RobotType), resolve = _ => ???)
    )
  )

  lazy val RobotType = ObjectType(
    name = "Robot",
    description = "A team's robot for a given year",
    fields = fields[Unit, Robot](
      Field("id", IDType, resolve = _ => ???),
      Field("team", TeamType, resolve = _ => ???),
      Field("game", ???, resolve = _ => ???),
      Field("name", OptionType(StringType), resolve = ???)
    )
  )

  lazy val DistrictType = ObjectType(
    name = "District",
    description = "A region which has enough teams that there is an extra competition tier",
    fields = fields[Unit, District](
      Field("id", IDType, resolve = _ => ???),
      Field("code", StringType, description = Some("Abbreviated name of the district"), resolve = _ => ???),
      Field("name", StringType, resolve = _ => ???),
      Field("gameType", StringType, resolve = _ => ???),
      Field("firstyear", IntType, resolve = _ => ???),
      Field("lastYear", OptionType(IntType), resolve = _ => ???),
      Field("teams", ListType(TeamType), resolve = _ => ???),
      Field("events", ListType(EventType), resolve = _ => ???)
    )
  )

  lazy val EventType = ObjectType(
    name = "Event",
    description = "An individual competition event",
    fields = fields[Unit, Event](
      Field("id", IDType, resolve = _ => ???),
      Field("district", OptionType(DistrictType), resolve = _ => ???),
      Field("game", GameType, resolve = _ => ???),
      Field("name", StringType, resolve = _ => ???),
      Field("address", OptionType(StringType), resolve = _ => ???),
      Field("startDate", OptionType(DateType), resolve = _ => ???),
      Field("endDate", OptionType(DateType), resolve = _ => ???),
      Field("teams", ListType(TeamType), resolve = _ => ???),
      Field("robots", ListType(RobotType), resolve = _ => ???),
      Field("matches", ListType(MatchType), resolve = _ => ???)
    )
  )

  lazy val GameType = ObjectType(
    name = "Game",
    description = "The game for a given year and game type (FRC, FTC, etc.)",
    fields = fields(
      Field("id", IDType, resolve = _ => ???),
      Field("name", StringType, resolve = _ => ???),
      Field("gameType", StringType, resolve = _ => ???),
      Field("year", YearType, resolve = _ => ???),
      Field("events", ListType(EventType), resolve = _ => ???)
    )
  )

  lazy val MatchType = ObjectType(
    name = "Match",
    description = "",
    fields = fields(
      Field("id", IDType, resolve = _ => ???),
      Field("event", EventType, resolve = _ => ???),
      Field("number", IntType, resolve = _ => ???),
      Field("type", StringType, resolve = _ => ???)
    )
  )

  val QueryType = ObjectType("Query", fields(
    Field("districts", ListType(DistrictType), resolve = _ => ???)
  ))

  val schema = Schema(QueryType)
}

