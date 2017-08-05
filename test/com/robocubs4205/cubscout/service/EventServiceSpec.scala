package com.robocubs4205.cubscout.service

import java.time.Year

import com.robocubs4205.cubscout.CubScoutDb
import com.robocubs4205.cubscout.controllers._
import com.robocubs4205.cubscout.model.{Event, Game}
import org.scalatest.BeforeAndAfter
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.db.slick.{DatabaseConfigProvider, DbName, SlickApi}
import play.api.test.Injecting
import slick.basic.{BasicProfile, DatabaseConfig}

import scala.concurrent._
import scala.concurrent.duration._
import language.postfixOps

/**
  * Created by trevor on 8/4/17.
  */
class EventServiceSpec extends PlaySpec with GuiceOneAppPerSuite with Injecting with BeforeAndAfter {
  implicit val ec: ExecutionContext = ExecutionContext.global

  val dbConfigProvider = new DatabaseConfigProvider {
    override def get[P <: BasicProfile]: DatabaseConfig[P] =
      inject[SlickApi].dbConfig[P](DbName("test"))
  }

  val csdb: CubScoutDb = new CubScoutDb(dbConfigProvider)
  val eventService = new EventService(csdb)

  import eventService._
  import csdb._
  import dbConfig._
  import profile.api._

  "findById" must {
    "return None if there are no events" in {
      Await.result(db.run(findById(1)), 20 seconds) mustBe empty
    }
    "return an event if one exists with the given id" in {
      val event = Event(1, None, 1, "foo", None, None, None)
      Await.result(db.run(events forceInsert event), 20 seconds)
      Await.result(db.run(findById(1)), 20 seconds) mustBe Some(event)
    }
    "not return an event if its id is not the given id" in {
      val event = Event(1, None, 1, "foo", None, None, None)
      Await.result(db.run(events forceInsert event), 20 seconds)
      Await.result(db.run(findById(12)), 20 seconds) mustBe None
    }
    "see the result of doInsert" in {
      val event = Event(1, None, 1, "foo", None, None, None)
      Await.result(db.run(doInsert(event)), 20 seconds)
      Await.result(db.run(findById(1)), 20 seconds) mustBe Some(event)
    }
    "see the result of doUpdate" in {
      val event = Event(1, None, 1, "foo", None, None, None)
      Await.result(db.run(events forceInsert event), 20 seconds)
      val newEvent = Event(1, None, 1, "bar", None, None, None)
      Await.result(db.run(doReplace(newEvent, 1)), 20 seconds)
      Await.result(db.run(findById(1)), 20 seconds) mustBe Some(newEvent)
    }
    "see the result of doDelete" in {
      val event = Event(1, None, 1, "foo", None, None, None)
      Await.result(db.run(events forceInsert event), 20 seconds)
      Await.result(db.run(doDelete(1)), 20 seconds)
      Await.result(db.run(findById(1)), 20 seconds) mustBe None
    }
  }

  "doInsert" must {
    "return the inserted event" in {
      val event = Event(1, None, 1, "foo", None, None, None)
      Await.result(db.run(doInsert(event)), 20 seconds) mustBe event
    }
  }

  "doReplace" must {
    "return the replaced event" in {
      val event = Event(1, None, 1, "foo", None, None, None)
      Await.result(db.run(events forceInsert event), 20 seconds)
      val newEvent = Event(1, None, 1, "bar", None, None, None)
      Await.result(db.run(doReplace(newEvent, 1)), 20 seconds) mustBe Some(newEvent)
    }
  }

  "doDelete" must {
    "return the deleted game" in {
      val event = Event(1, None, 1, "foo", None, None, None)
      Await.result(db.run(events forceInsert event), 20 seconds)
      Await.result(db.run(doDelete(1)), 20 seconds) mustBe Some(event)
    }
  }

  "checkNoInsertConflict" must {
    "return success if there are no events and the given game and district exist" in {
      val event = Event(1, None, 1, "foo", None, None, None)
      val future = db.run(checkNoInsertConflict(event))
      Await.ready(future, 20 seconds)
      future.value.get.isSuccess mustBe true
    }
    "return success if there is an event with the same name and a different game and the given game and district exist" in {
      val existingEvent = Event(1, None, 2, "foo", None, None, None)
      val gameForExistingEvent = Game(2, "bar", "FTC", Year.of(2531))
      Await.result(
        db.run {
          games forceInsert gameForExistingEvent andThen
            (events += existingEvent)
        },
        20 seconds
      )
      val event = Event(1, None, 1, "foo", None, None, None)
      val future = db.run(checkNoInsertConflict(event))
      Await.ready(future, 20 seconds)
      future.value.get.isSuccess mustBe true
    }
    "return success if there is an event with the same game and a different name and the given game and district exist" in {
      val existingEvent = Event(1, None, 1, "bar", None, None, None)
      Await.result(db.run(events += existingEvent), 20 seconds)
      val event = Event(1, None, 1, "foo", None, None, None)
      val future = db.run(checkNoInsertConflict(event))
      Await.ready(future, 20 seconds)
      future.value.get.isSuccess mustBe true
    }
    "return failure with EventAlreadyExistsException if there is an event with the same name and game and the given game and district exist" in {
      val existingEvent = Event(1, None, 1, "foo", None, None, None)
      Await.result(db.run(events += existingEvent), 20 seconds)
      val event = Event(1, None, 1, "foo", None, None, None)
      val future = db.run(checkNoInsertConflict(event))
      Await.ready(future, 20 seconds)
      future.value.get.isSuccess mustBe false
      future.failed.map(_ must matchPattern {
        case _: EventAlreadyExistsException =>
      })
    }
    "return failure with GameNotFoundException if there are no events, the given game does not exist, and the given district exists" in {
      Await.result(db.run(games.delete),20 seconds)
      val event = Event(1, None, 1, "foo", None, None, None)
      val future = db.run(checkNoInsertConflict(event))
      Await.ready(future, 20 seconds)
      future.value.get.isSuccess mustBe false
      future.failed.map(_ must matchPattern {
        case _: GameNotFoundException =>
      })
    }
    "return failure with DistrictNotFoundException if there are no events, the given game exists, and the given district does not exist" in {
      Await.result(db.run(districts.delete),20 seconds)
      val event = Event(1, Some(1), 1, "foo", None, None, None)
      val future = db.run(checkNoInsertConflict(event))
      Await.ready(future, 20 seconds)
      future.value.get.isSuccess mustBe false
      future.failed.map(_ must matchPattern {
        case _: DistrictNotFoundException =>
      })
    }
    "return success if there are no events, the given game exists, and there is no given district" in {
      Await.result(db.run(districts.delete),20 seconds)
      val event = Event(1, None, 1, "foo", None, None, None)
      val future = db.run(checkNoInsertConflict(event))
      Await.ready(future, 20 seconds)
      future.value.get.isSuccess mustBe true
    }
  }

  before {
    Await.result(
      db.run {
        games.schema.create andThen
          districts.schema.create andThen
          events.schema.create andThen
          (games forceInsert Game(1, "fooGame", "FRC", Year.of(2017)))
      },
      20 seconds
    )
  }

  after {
    Await.result(
      db.run {
        events.schema.drop andThen
          districts.schema.drop andThen
          games.schema.drop
      },
      20 seconds
    )
  }
}
