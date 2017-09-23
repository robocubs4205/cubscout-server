package com.robocubs4205.cubscout

import java.sql.{Date, Timestamp}
import java.time.temporal.ChronoField
import java.time.{Instant, LocalDate, Year}
import java.util.UUID
import javax.inject.{Inject, Named}

import com.netaporter.uri.Uri
import com.robocubs4205.cubscout.model._
import com.robocubs4205.cubscout.model.scorecard.Result
import com.robocubs4205.cubscout.model.access.AccessToken.{AccessTokenWithRefreshToken, StandaloneAccessToken}
import com.robocubs4205.cubscout.model.access._
import com.robocubs4205.cubscout.model.access.Client._
import com.robocubs4205.cubscout.model.access.{AuthCode, RefreshToken, User}
import play.api.{Application, Environment, Logger}
import play.api.Mode.Test
import play.api.db.slick.{DatabaseConfigProvider, DbName, DefaultSlickApi, SlickApi}
import play.api.libs.json.{JsValue, Json}
import slick.jdbc.JdbcProfile
import slick.model.ForeignKeyAction.{Cascade, Restrict}

import scala.concurrent.ExecutionContext


/**
  * Created by trevor on 7/20/17.
  */
class CubScoutDb @Inject()(dbConfigProvider: DatabaseConfigProvider)(implicit ex: ExecutionContext) {
  val dbConfig = dbConfigProvider.get[JdbcProfile]


  import dbConfig._
  import profile.api._

  DBIO.successful("")

  implicit val localDateMapping = MappedColumnType.base[LocalDate, Date](
    Date.valueOf,
    _.toLocalDate
  )

  implicit val yearMapping = MappedColumnType.base[Year, Int](
    _.get(ChronoField.YEAR),
    Year.of
  )

  implicit val jsonMapping = MappedColumnType.base[JsValue, String](
    _.toString(),
    Json.parse
  )

  implicit val uriMapping = MappedColumnType.base[Seq[Uri], String](
    Json.toJson(_).toString,
    s => Json.fromJson[Seq[Uri]](Json.parse(s)).get
  )

  implicit val instantMapping = MappedColumnType.base[Instant, java.sql.Timestamp](
    i => new Timestamp(i.get(ChronoField.MILLI_OF_SECOND)),
    _.toInstant
  )

  implicit val TokenValMapping = MappedColumnType.base[TokenVal, String](
    _.toString,
    TokenVal(_).get
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

    def uniqueGameName = index("game_name_ux_events", (gameId, name), unique = true)

    def * = (id, districtId, gameId, name, address, startDate, endDate) <> (Event.tupled, Event.unapply)
  }

  class GameTable(tag: Tag) extends Table[Game](tag, "games") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def name = column[String]("name", O.Unique)

    def `type` = column[String]("type")

    def year = column[Year]("year")

    def uniqueTypeYear = index("type_year_ux_games", (`type`, year), unique = true)

    def * = (id, name, `type`, year) <> (Game.tupled, Game.unapply)
  }

  class MatchTable(tag: Tag) extends Table[Match](tag, "matches") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def eventId = column[Long]("eventId")

    def eventFk = foreignKey("match_event_fk", eventId, events)(_.id, onUpdate = Restrict, onDelete = Restrict)

    def number = column[Long]("number")

    def `type` = column[String]("type")

    def uniqueEventNumberType = index("event_number_type_ux_matches", (eventId, number, `type`), unique = true)

    def * = (id, eventId, number, `type`) <> (Match.tupled, Match.unapply)
  }

  class TeamTable(tag: Tag) extends Table[Team](tag, "teams") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def number = column[Long]("number")

    def name = column[Option[String]]("name")

    def gameType = column[String]("gameType")

    def districtId = column[Option[Long]]("districtId")

    def districtFk = foreignKey("team_district_fk", districtId, districts)(_.id.?, onUpdate = Restrict, onDelete = Restrict)

    def uniqueNumberGameType = index("number_gameType_ux_teams", (number, gameType), unique = true)

    def * = (id, number, name, gameType, districtId) <> (Team.tupled, Team.unapply)
  }

  class RobotTable(tag: Tag) extends Table[Robot](tag, "robots") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def teamId = column[Long]("teamId")

    def teamFk = foreignKey("robot_team_fk", teamId, teams)(_.id, onUpdate = Restrict, onDelete = Restrict)

    def gameId = column[Long]("gameId")

    def gameFk = foreignKey("robot_game_fk", gameId, games)(_.id, onUpdate = Restrict, onDelete = Restrict)

    def name = column[Option[String]]("name")

    def uniqueTeamGame = index("team_game_ux_robots", (teamId, gameId), unique = true)

    def * = (id, teamId, gameId, name) <> (Robot.tupled, Robot.unapply)
  }

  class ResultTable(tag: Tag) extends Table[Result](tag, "results") {
    def id = column[Long]("id", O.AutoInc, O.PrimaryKey)

    def robotId = column[Long]("teamId")

    def robotFk = foreignKey("result_team_fk", robotId, robots)(_.id, onUpdate = Restrict, onDelete = Restrict)

    def matchId = column[Long]("matchId")

    def matchFk = foreignKey("result_match_fk", matchId, matches)(_.id, onUpdate = Restrict, onDelete = Restrict)

    def scorecardId = column[Long]("scorecardId")

    def pk = primaryKey("result_pk", (robotId, matchId))

    def * = (id, robotId, matchId, scorecardId) <> (Result.tupled, Result.unapply)
  }

  class UserTable(tag: Tag) extends Table[User](tag, "users") {
    def id = column[TokenVal]("id", O.PrimaryKey)

    def username = column[String]("username", O.Unique)

    def hashedPassword = column[String]("hashedPassword")

    def * = (id, username, hashedPassword) <> (User.tupled, User.unapply)
  }

  class ServerClientsTable(tag: Tag) extends Table[ServerClient](tag, "serverClients") {
    def id = column[TokenVal]("id", O.PrimaryKey)

    def name = column[String]("name")

    def author = column[String]("name")

    def secret = column[TokenVal]("secret")

    def redirectUris = column[Seq[Uri]]("uris")

    def firstParty = column[Boolean]("firstParty")

    def * = (id, name, author, secret, redirectUris, firstParty) <> (ServerClient.tupled, ServerClient.unapply)
  }

  class BrowserClientsTable(tag: Tag) extends Table[BrowserClient](tag, "browserClients") {
    def id = column[TokenVal]("id", O.PrimaryKey)

    def name = column[String]("name")

    def author = column[String]("name")

    def redirectUris = column[Seq[Uri]]("uris")

    def firstParty = column[Boolean]("firstParty")

    def * = (id, name, author, redirectUris, firstParty) <> (BrowserClient.tupled, BrowserClient.unapply)
  }

  class NativeClientsTable(tag: Tag) extends Table[NativeClient](tag, "nativeClients") {
    def id = column[TokenVal]("id", O.PrimaryKey)

    def name = column[String]("name")

    def author = column[String]("name")

    def redirectUris = column[Seq[Uri]]("uris")

    def firstParty = column[Boolean]("firstParty")

    def * = (id, name, author, redirectUris, firstParty) <> (NativeClient.tupled, NativeClient.unapply)
  }

  class RefreshTokenTable(tag: Tag) extends Table[RefreshToken](tag, "refreshTokens") {
    def selector = column[TokenVal]("selector", O.PrimaryKey)

    def validator = column[TokenVal]("validator")

    //no foreign key since clients are spread across multiple tables
    def clientId = column[TokenVal]("clientId")

    def userId = column[TokenVal]("userId")

    def userFk = foreignKey("refreshToken_user_fk", userId, users)(_.id, onUpdate = Cascade, onDelete = Cascade)

    def scopes = column[String]("scopes")

    private[this] def makeToken(selector: TokenVal, validator: TokenVal, clientId: TokenVal, userId: TokenVal, scopes: String) =
      Scope.parseSet(scopes).map(
        scopes => RefreshToken(selector, validator, clientId, userId, scopes)
      ).get

    private[this] def explodeToken(token: RefreshToken) =
      Some((token.selector, token.validator, token.clientId, token.userId, Scope.toString(token.scopes)))

    def * = (selector, validator, clientId, userId, scopes) <> ((makeToken _).tupled, explodeToken)
  }

  class AccessTokenWithRefreshTokenTable(tag: Tag)
    extends Table[AccessTokenWithRefreshToken](tag, "accessTokensWithRefreshTokens") {
    def selector = column[TokenVal]("selector", O.PrimaryKey)

    def validator = column[TokenVal]("validator")

    def refreshTokenSelector = column[TokenVal]("refreshTokenSelector")

    def refreshTokenFk = foreignKey(
      "accessToken_refreshToken_fk", refreshTokenSelector, refreshTokens
    )(_.selector, onUpdate = Cascade, onDelete = Cascade)

    def created = column[Instant]("created")

    def * = (selector, validator, refreshTokenSelector, created) <> (AccessTokenWithRefreshToken.tupled, AccessTokenWithRefreshToken.unapply)
  }

  class StandaloneAccessTokenTable(tag: Tag) extends Table[StandaloneAccessToken](tag, "standaloneAccessTokens") {
    def selector = column[TokenVal]("selector", O.PrimaryKey)

    def validator = column[TokenVal]("validator")

    def clientId = column[TokenVal]("clientId")

    def userId = column[TokenVal]("userId")

    def userFk = foreignKey("standaloneAccessToken_user_fk", userId, users)(_.id, onUpdate = Cascade, onDelete = Cascade)

    def created = column[Instant]("created")

    def scopes = column[String]("scopes")

    private[this] def makeToken(selector: TokenVal, validator: TokenVal, clientId: TokenVal, userId: TokenVal, scopes: String, created: Instant) =
      Scope.parseSet(scopes).map(
        scopes => StandaloneAccessToken(selector, validator, clientId, userId, scopes, created)
      ).get

    private[this] def explodeToken(token: StandaloneAccessToken) =
      Some((token.selector, token.validator, token.clientId, token.userId, Scope.toString(token.scopes), token.created))

    def * = (selector, validator, clientId, userId, scopes, created) <> ((makeToken _).tupled, explodeToken)
  }

  class AuthCodeTable(tag: Tag) extends Table[AuthCode](tag, "authCodes") {
    def selector = column[TokenVal]("selector", O.PrimaryKey)

    def validator = column[TokenVal]("validator")

    def clientId = column[TokenVal]("clientId")

    def userId = column[TokenVal]("userId")

    def userFk = foreignKey("authCode_user_fk", userId, users)(_.id, onUpdate = Cascade, onDelete = Cascade)

    def redirectUrl = column[String]("redirectURL")

    def * = (selector, validator, clientId, userId, redirectUrl) <> (AuthCode.tupled, AuthCode.unapply)
  }

  val districts = TableQuery[DistrictTable]
  val events = TableQuery[EventTable]
  val games = TableQuery[GameTable]
  val matches = TableQuery[MatchTable]
  val teams = TableQuery[TeamTable]
  val robots = TableQuery[RobotTable]
  val results = TableQuery[ResultTable]

  val users = TableQuery[UserTable]
  val serverClients = TableQuery[ServerClientsTable]
  val browserClients = TableQuery[BrowserClientsTable]
  val nativeClients = TableQuery[NativeClientsTable]

  val refreshTokens = TableQuery[RefreshTokenTable]
  val accessTokensWithRefreshTokens = TableQuery[AccessTokenWithRefreshTokenTable]
  val standaloneAccessTokens = TableQuery[StandaloneAccessTokenTable]
  val authCodes = TableQuery[AuthCodeTable]
}
