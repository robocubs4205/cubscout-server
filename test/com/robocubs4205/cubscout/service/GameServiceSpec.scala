package com.robocubs4205.cubscout.service

import java.time.Year

import com.robocubs4205.cubscout.CubScoutDb
import com.robocubs4205.cubscout.model._
import org.scalatest.BeforeAndAfter
import org.scalatestplus.play._
import org.scalatestplus.play.guice._
import play.api.Logger
import play.api.test._
import play.api.db.slick.{DatabaseConfigProvider, DbName, SlickApi}
import slick.basic.{BasicProfile, DatabaseConfig}

import scala.concurrent._
import scala.concurrent.duration._
import language.postfixOps

class GameServiceSpec extends PlaySpec with GuiceOneAppPerSuite with Injecting with BeforeAndAfter {
  implicit val ec: ExecutionContext = ExecutionContext.global

  val dbConfigProvider = new DatabaseConfigProvider {
    override def get[P <: BasicProfile]: DatabaseConfig[P] =
      inject[SlickApi].dbConfig[P](DbName("test"))
  }

  val csdb: CubScoutDb = new CubScoutDb(dbConfigProvider)
  val gameService = new GameService(csdb)

  import gameService._
  import csdb._
  import dbConfig._
  import profile.api._

  "findById" must {
    "return None if there are no games" in {
      Await.result(db.run(findById(1)), 20 seconds) mustBe empty
    }
    "return a game if one exists with the given id" in {
      val game = Game(1, "foo", "foo", Year.of(2017))
      Await.result(db.run(games forceInsert game), 20 seconds)
      Await.result(db.run(findById(1)), 20 seconds) mustBe Some(game)
    }
    "not return a game if its id is not the given id" in {
      val game = Game(2, "foo", "foo", Year.of(2017))
      Await.result(db.run(games forceInsert game), 20 seconds)

      Await.result(db.run(findById(1)), 20 seconds) mustBe empty
    }
    "see the result of doInsert" in {
      val game = Game(1, "foo", "bar", Year.of(10))
      Await.result(db.run(doInsert(game)), 20 seconds)
      Await.result(db.run(findById(1)), 20 seconds) mustBe Some(game)
    }
    "see the result of doUpdate" in {
      val game = Game(1, "foo", "foo", Year.of(2017))
      Await.result(db.run(games forceInsert game), 20 seconds)
      val newGame = Game(1, "bar", "baz", Year.of(2531))
      Await.result(db.run(doReplace(newGame, 1)), 20 seconds)
      Await.result(db.run(findById(1)), 20 seconds) mustBe Some(newGame)
    }
    "see the result of doDelete" in {
      val game = Game(1, "foo", "foo", Year.of(2017))
      Await.result(db.run(games forceInsert game), 20 seconds)
      Await.result(db.run(doDelete(1)),20 seconds)
      Await.result(db.run(findById(1)), 20 seconds) mustBe None
    }
  }

  "doInsert" must {
    "return the inserted game" in {
      val game = Game(1, "foo", "bar", Year.of(10))
      Await.result(db.run(doInsert(game)), 20 seconds) mustBe game
    }
  }

  "doReplace" must {
    "return the replaced game" in {
      val game = Game(1, "foo", "foo", Year.of(2017))
      Await.result(db.run(games forceInsert game), 20 seconds)
      val newGame = Game(1, "bar", "baz", Year.of(2531))
      Await.result(db.run(doReplace(newGame, 1)), 20 seconds) mustBe Some(newGame)
    }
  }

  "doDelete" must {
    "return the deleted game" in {
      val game = Game(1, "foo", "foo", Year.of(2017))
      Await.result(db.run(games forceInsert game), 20 seconds)
      Await.result(db.run(doDelete(1)), 20 seconds) mustBe Some(game)
    }
  }

  "checkNoInsertConflict" must {
    "return success if there are no games" in {
      val game = Game(1, "foo", "foo", Year.of(2017))
      val future = db.run(checkNoInsertConflict(game))
      Await.ready(future, 20 seconds)
      future.value.get.isSuccess mustBe true
    }
    "return success if there is only an existing game with a different name, type, and year" in {
      val existingGame = Game(1, "bar", "baz", Year.of(2531))
      Await.result(db.run(games += existingGame), 20 seconds)
      val game = Game(1, "foo", "foo", Year.of(2017))
      val future = db.run(checkNoInsertConflict(game))
      Await.ready(future, 20 seconds)
      future.value.get.isSuccess mustBe true
    }
    "return success if there is only an existing game with the same year and a different name and type" in {
      val existingGame = Game(1, "bar", "baz", Year.of(2017))
      Await.result(db.run(games += existingGame), 20 seconds)
      val game = Game(1, "foo", "foo", Year.of(2017))
      val future = db.run(checkNoInsertConflict(game))
      Await.ready(future, 20 seconds)
      future.value.get.isSuccess mustBe true
    }
    "return success if there is only an existing game with the same type and a different name and year" in {
      val existingGame = Game(1, "bar", "foo", Year.of(2531))
      Await.result(db.run(games += existingGame), 20 seconds)
      val game = Game(1, "foo", "foo", Year.of(2017))
      val future = db.run(checkNoInsertConflict(game))
      Await.ready(future, 20 seconds)
      future.value.get.isSuccess mustBe true
    }
    "return failure if there is an identical existing game" in {
      val game = Game(1, "foo", "foo", Year.of(2017))
      Await.result(db.run(games += game), 20 seconds)
      val future = db.run(checkNoInsertConflict(game))
      Await.ready(future, 20 seconds)
      future.value.get.isFailure mustBe true
    }
    "return failure with GameWithSameNameExistsException if there is an existing game with the same name and a different type and year" in {
      val existingGame = Game(1, "foo", "baz", Year.of(2531))
      Await.result(db.run(games += existingGame), 20 seconds)
      val game = Game(1, "foo", "foo", Year.of(2017))
      val future = db.run(checkNoInsertConflict(game))
      Await.ready(future, 20 seconds)
      future.value.get.isFailure mustBe true
      future.value.get.failed.map(_ must matchPattern{
        case _:GameWithSameNameExistsException =>
      })
    }
    "return failure with GameWithSameNameExistsException if there is an existing game with the same name and type and a different year" in {
      val existingGame = Game(1, "foo", "foo", Year.of(2531))
      Await.result(db.run(games += existingGame), 20 seconds)
      val game = Game(1, "foo", "foo", Year.of(2017))
      val future = db.run(checkNoInsertConflict(game))
      Await.ready(future, 20 seconds)
      future.value.get.isFailure mustBe true
      future.value.get.failed.map(_ must matchPattern{
        case _:GameWithSameNameExistsException =>
      })
    }
    "return failure with GameWithSameNameExistsException if there is an existing game with the same name and year and a different type" in {
      val existingGame = Game(1, "foo", "bar", Year.of(2017))
      Await.result(db.run(games += existingGame), 20 seconds)
      val game = Game(1, "foo", "foo", Year.of(2017))
      val future = db.run(checkNoInsertConflict(game))
      Await.ready(future, 20 seconds)
      future.value.get.isFailure mustBe true
      future.value.get.failed.map(_ must matchPattern{
        case _:GameWithSameNameExistsException =>
      })
    }
    "return failure with GameWithSameYearAndTypeException if there is an existing game with the same type and year and a different name" in {
      val existingGame = Game(1, "bar", "foo", Year.of(2017))
      Await.result(db.run(games += existingGame), 20 seconds)
      val game = Game(1, "foo", "foo", Year.of(2017))
      val future = db.run(checkNoInsertConflict(game))
      Await.ready(future, 20 seconds)
      future.value.get.isFailure mustBe true
      future.value.get.failed.map(_ must matchPattern{
        case _:GameWithSameYearAndTypeException =>
      })
    }
  }

  "checkNoReplaceConflict" must {
    "return success if there are no games" in {
      val game = Game(1, "foo", "foo", Year.of(2017))
      val future = db.run(checkNoReplaceConflict(game, 1))
      Await.ready(future, 20 seconds)
      future.value.get.isSuccess mustBe true
    }
    "return success if there is only an existing game with a different name, type, and year" in {
      val existingGame = Game(2, "bar", "baz", Year.of(2531))
      Await.result(db.run(games forceInsert existingGame), 20 seconds)
      val game = Game(1, "foo", "foo", Year.of(2017))
      val future = db.run(checkNoReplaceConflict(game, 1))
      Await.ready(future, 20 seconds)
      future.value.get.isSuccess mustBe true
    }
    "return success if there is only an existing game with the same year and a different name and type" in {
      val existingGame = Game(2, "bar", "baz", Year.of(2017))
      Await.result(db.run(games forceInsert existingGame), 20 seconds)
      val game = Game(1, "foo", "foo", Year.of(2017))
      val future = db.run(checkNoReplaceConflict(game, 1))
      Await.ready(future, 20 seconds)
      future.value.get.isSuccess mustBe true
    }
    "return success if there is only an existing game with the same type and a different name and year" in {
      val existingGame = Game(2, "bar", "foo", Year.of(2531))
      Await.result(db.run(games forceInsert existingGame), 20 seconds)
      val game = Game(1, "foo", "foo", Year.of(2017))
      val future = db.run(checkNoReplaceConflict(game, 1))
      Await.ready(future, 20 seconds)
      future.value.get.isSuccess mustBe true
    }
    "return failure if there is an identical existing game with a different id" in {
      val game = Game(2, "foo", "foo", Year.of(2017))
      Await.result(db.run(games forceInsert game), 20 seconds)
      val future = db.run(checkNoReplaceConflict(game.copy(id = 1), 1))
      Await.ready(future, 20 seconds)
      future.value.get.isFailure mustBe true
    }
    "return failure with GameWithSameNameExistsException if there is an existing game with the same name and a different type, year and id" in {
      val existingGame = Game(2, "foo", "baz", Year.of(2531))
      Await.result(db.run(games forceInsert existingGame), 20 seconds)
      val game = Game(1, "foo", "foo", Year.of(2017))
      val future = db.run(checkNoReplaceConflict(game, 1))
      Await.ready(future, 20 seconds)
      future.value.get.isFailure mustBe true
      future.failed.map(_ must matchPattern{
        case _:GameWithSameNameExistsException =>
      })
    }
    "return failure with GameWithSameNameExistsException if there is an existing game with the same name and type and a different year and id" in {
      val existingGame = Game(2, "foo", "foo", Year.of(2531))
      Await.result(db.run(games forceInsert existingGame), 20 seconds)
      val game = Game(1, "foo", "foo", Year.of(2017))
      val future = db.run(checkNoReplaceConflict(game, 1))
      Await.ready(future, 20 seconds)
      future.value.get.isFailure mustBe true
      future.failed.map(_ must matchPattern{
        case _:GameWithSameNameExistsException =>
      })
    }
    "return failure with GameWithSameNameExistsException if there is an existing game with the same name and year and a different type and id" in {
      val existingGame = Game(2, "foo", "bar", Year.of(2017))
      Await.result(db.run(games forceInsert existingGame), 20 seconds)
      val game = Game(1, "foo", "foo", Year.of(2017))
      val future = db.run(checkNoReplaceConflict(game, 1))
      Await.ready(future, 20 seconds)
      future.value.get.isFailure mustBe true
      future.failed.map(_ must matchPattern{
        case _:GameWithSameNameExistsException =>
      })
    }
    "return failure with GameWithSameYearAndTypeException if there is an existing game with the same type and year and a different name and id" in {
      val existingGame = Game(2, "bar", "foo", Year.of(2017))
      Await.result(db.run(games forceInsert existingGame), 20 seconds)
      val game = Game(1, "foo", "foo", Year.of(2017))
      val future = db.run(checkNoReplaceConflict(game, 1))
      Await.ready(future, 20 seconds)
      future.value.get.isFailure mustBe true
      future.failed.map(_ must matchPattern{
        case _:GameWithSameYearAndTypeException =>
      })
    }
    "return success if there is only an identical existing game with the same id" in {
      val game = Game(1, "foo", "foo", Year.of(2017))
      Await.result(db.run(games forceInsert game), 20 seconds)
      val future = db.run(checkNoReplaceConflict(game, 1))
      Await.ready(future, 20 seconds)
      future.value.get.isSuccess mustBe true
    }
    "return success if there is only an existing game with the same name and id and a different type and year" in {
      val existingGame = Game(1, "foo", "baz", Year.of(2531))
      Await.result(db.run(games forceInsert existingGame), 20 seconds)
      val game = Game(1, "foo", "foo", Year.of(2017))
      val future = db.run(checkNoReplaceConflict(game, 1))
      Await.ready(future, 20 seconds)
      future.value.get.isSuccess mustBe true
    }
    "return success if there is only an existing game with the same name, type and id and a different year" in {
      val existingGame = Game(1, "foo", "foo", Year.of(2531))
      Await.result(db.run(games forceInsert existingGame), 20 seconds)
      val game = Game(1, "foo", "foo", Year.of(2017))
      val future = db.run(checkNoReplaceConflict(game, 1))
      Await.ready(future, 20 seconds)
      future.value.get.isSuccess mustBe true
    }
    "return success if there is only an existing game with the same name, year and id and a different type" in {
      val existingGame = Game(1, "foo", "bar", Year.of(2017))
      Await.result(db.run(games forceInsert existingGame), 20 seconds)
      val game = Game(1, "foo", "foo", Year.of(2017))
      val future = db.run(checkNoReplaceConflict(game, 1))
      Await.ready(future, 20 seconds)
      future.value.get.isSuccess mustBe true
    }
    "return success if there is only an existing game with the same type, year and id and a different name" in {
      val existingGame = Game(1, "bar", "foo", Year.of(2017))
      Await.result(db.run(games forceInsert existingGame), 20 seconds)
      val game = Game(1, "foo", "foo", Year.of(2017))
      val future = db.run(checkNoReplaceConflict(game, 1))
      Await.ready(future, 20 seconds)
      future.value.get.isSuccess mustBe true
    }
  }

  "checkNoDeleteConflict" must {
    "return success if this game has no events and no robots" in {
      val game = Game(1, "foo", "foo", Year.of(2017))
      Await.result(db.run(games forceInsert game), 20 seconds)
      val future = db.run(checkNoDeleteConflict(1))
      Await.ready(future, 20 seconds)
      future.value.get.isSuccess mustBe true
    }
    "return failure with EventsInGameException if this game has one event and no robots" in {
      val game = Game(1, "foo", "foo", Year.of(2017))
      val district = District(1, "FOO", "baz", "FRC", Year.of(1), None)
      val event = Event(1, Some(1), 1, "bar", None, None, None)
      Await.result(
        db.run {
          games forceInsert game andThen
            (districts forceInsert district) andThen
            (events forceInsert event)
        },
        20 seconds
      )
      val future = db.run(checkNoDeleteConflict(1))
      Await.ready(future, 20 seconds)
      future.value.get.isFailure mustBe true
      future.failed.map(_ must matchPattern{
        case _:EventsInGameException =>
      })
    }
    "return failure with RobotsInGameException if this game has no events and one robot" in {
      val game = Game(1, "foo", "foo", Year.of(2017))
      val team = Team(1, 1, None, "FRC", None)
      val robot = Robot(1, 1, 1, None)
      Await.result(
        db.run {
          games forceInsert game andThen
            (teams forceInsert team) andThen
            (robots forceInsert robot)
        },
        20 seconds
      )
      val future = db.run(checkNoDeleteConflict(1))
      Await.ready(future, 20 seconds)
      future.value.get.isFailure mustBe true
      future.failed.map(_ must matchPattern{
        case _:RobotsInGameException =>
      })
    }
    "return failure if this game has one event and one robot" in {
      val game = Game(1, "foo", "foo", Year.of(2017))
      val district = District(1, "FOO", "baz", "FRC", Year.of(1), None)
      val event = Event(1, Some(1), 1, "bar", None, None, None)
      val team = Team(1, 1, None, "FRC", None)
      val robot = Robot(1, 1, 1, None)
      Await.result(
        db.run {
          games forceInsert game andThen
            (districts forceInsert district) andThen
            (events forceInsert event) andThen
            (teams forceInsert team) andThen
            (robots forceInsert robot)
        },
        20 seconds
      )
      val future = db.run(checkNoDeleteConflict(1))
      Await.ready(future, 20 seconds)
      future.value.get.isFailure mustBe true
    }
  }

  before {
    Await.result(
      db.run {
        games.schema.create andThen
          districts.schema.create andThen
          events.schema.create andThen
          teams.schema.create andThen
          robots.schema.create
      },
      20 seconds
    )
  }

  after {
    Await.result(
      db.run {
        robots.schema.drop andThen
          teams.schema.drop andThen
          events.schema.drop andThen
          districts.schema.drop andThen
          games.schema.drop
      },
      20 seconds
    )
  }
}
