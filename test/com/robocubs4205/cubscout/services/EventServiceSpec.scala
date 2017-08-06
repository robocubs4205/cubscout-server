package com.robocubs4205.cubscout.services

import java.time.Year

import com.robocubs4205.cubscout.CubScoutDb
import com.robocubs4205.cubscout.controllers._
import com.robocubs4205.cubscout.model.{District, Event, Game, Match}
import org.scalatest.BeforeAndAfter
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.db.slick.{DatabaseConfigProvider, DbName, SlickApi}
import play.api.test.Injecting
import play.api.Logger
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
    val districtForEvent = District(1, "FOO", "fooDistrict", "FRC", Year.of(2531), None)
    val gameForEvent = Game(1, "fooGame", "FRC", Year.of(2531))
    "return None if there are no events" in {
      Await.result(db.run(findById(1)), 20 seconds) mustBe empty
    }
    "return an event if one exists with the given id" in {
      val event = Event(1, None, 1, "foo", None, None, None)
      Await.result(
        db.run {
          (districts forceInsert districtForEvent) andThen
            (games forceInsert gameForEvent) andThen
            (events forceInsert event)
        },
        20 seconds
      )

      Await.result(db.run(findById(1)), 20 seconds) mustBe Some(event)
    }
    "not return an event if its id is not the given id" in {
      val event = Event(1, None, 1, "foo", None, None, None)
      Await.result(
        db.run {
          (districts forceInsert districtForEvent) andThen
            (games forceInsert gameForEvent) andThen
            (events forceInsert event)
        },
        20 seconds
      )

      Await.result(db.run(findById(12)), 20 seconds) mustBe None
    }
    "see the result of doInsert" in {
      val event = Event(1, None, 1, "foo", None, None, None)
      Await.result(
        db.run {
          (districts forceInsert districtForEvent) andThen
            (games forceInsert gameForEvent) andThen
            (events forceInsert event)
        },
        20 seconds
      )

      Await.result(db.run(findById(1)), 20 seconds) mustBe Some(event)
    }
    "see the result of doUpdate" in {
      val event = Event(1, None, 1, "foo", None, None, None)
      Await.result(
        db.run {
          (districts forceInsert districtForEvent) andThen
            (games forceInsert gameForEvent) andThen
            (events forceInsert event)
        },
        20 seconds
      )
      val newEvent = Event(1, None, 1, "bar", None, None, None)
      Await.result(db.run(doReplace(newEvent, 1)), 20 seconds)

      Await.result(db.run(findById(1)), 20 seconds) mustBe Some(newEvent)
    }
    "see the result of doDelete" in {
      val event = Event(1, None, 1, "foo", None, None, None)
      Await.result(
        db.run {
          (districts forceInsert districtForEvent) andThen
            (games forceInsert gameForEvent) andThen
            (events forceInsert event)
        },
        20 seconds
      )
      Await.result(db.run(doDelete(1)), 20 seconds)

      Await.result(db.run(findById(1)), 20 seconds) mustBe None
    }
  }

  "doInsert" must {
    "return the inserted event" in {
      val districtForEvent = District(1, "FOO", "fooDistrict", "FRC", Year.of(2531), None)
      val gameForEvent = Game(1, "fooGame", "FRC", Year.of(2531))
      Await.result(
        db.run {
          (games forceInsert gameForEvent) andThen
            (districts forceInsert districtForEvent)
        },
        20 seconds
      )
      val event = Event(1, None, 1, "foo", None, None, None)
      Await.result(db.run(doInsert(event)), 20 seconds) mustBe event
    }
  }

  "doReplace" must {
    "return the replaced event" in {
      val event = Event(1, None, 1, "foo", None, None, None)
      val districtForEvent = District(1, "FOO", "fooDistrict", "FRC", Year.of(2531), None)
      val gameForEvent = Game(1, "fooGame", "FRC", Year.of(2531))
      Await.result(
        db.run {
          (games forceInsert gameForEvent) andThen
            (districts forceInsert districtForEvent) andThen
            (events forceInsert event)
        },
        20 seconds
      )
      val newEvent = Event(1, None, 1, "bar", None, None, None)
      Await.result(db.run(doReplace(newEvent, 1)), 20 seconds) mustBe Some(newEvent)
    }
  }

  "doDelete" must {
    "return the deleted game" in {
      val event = Event(1, None, 1, "foo", None, None, None)
      val districtForEvent = District(1, "FOO", "fooDistrict", "FRC", Year.of(2531), None)
      val gameForEvent = Game(1, "fooGame", "FRC", Year.of(2531))
      Await.result(
        db.run {
          (games forceInsert gameForEvent) andThen
            (districts forceInsert districtForEvent) andThen
            (events forceInsert event)
        },
        20 seconds
      )

      Await.result(db.run(doDelete(1)), 20 seconds) mustBe Some(event)
    }
  }

  "checkNoInsertConflict" when {
    "checking an event with a district" when {
      val newEvent = Event(1, Some(1), 1, "foo", None, None, None)
      "there are no events" when {
        "the given game exists" when {
          val gameForNewEvent = Game(1, "fooGame", "FRC", Year.of(2017))
          "the given district exists" must {
            val districtForNewEvent = District(1, "FOO", "fooDistrict", "FRC", Year.of(2531), None)
            "return success" in {
              Await.result(
                db.run {
                  (games forceInsert gameForNewEvent) andThen
                    (districts forceInsert districtForNewEvent)
                },
                20 seconds
              )

              val future = db.run(checkNoInsertConflict(newEvent))

              Await.ready(future, 20 seconds)
              future.value.get.isSuccess mustBe true
            }
          }
          "the given district does not exist" must {
            "return failure with DistrictNotFoundException" in {
              Await.result(db.run(games forceInsert gameForNewEvent), 20 seconds)

              val future = db.run(checkNoInsertConflict(newEvent))

              Await.ready(future, 20 seconds)
              future.value.get.isFailure mustBe true
              future.failed.map(_ must matchPattern {
                case _: DistrictNotFoundException =>
              })
            }
          }
        }
        "the given game does not exist" when {
          "the given district exists" must {
            "return failure with GameNotFoundException" in {
              val districtForNewEvent = District(1, "FOO", "fooDistrict", "FRC", Year.of(2531), None)
              Await.result(
                db.run(districts forceInsert districtForNewEvent),
                20 seconds
              )

              val future = db.run(checkNoInsertConflict(newEvent))

              Await.ready(future, 20 seconds)
              future.value.get.isFailure mustBe true
              future.failed.map(_ must matchPattern {
                case _: GameNotFoundException =>
              })
            }
          }
          "the given district does not exist" must {
            "return failure" in {
              Await.result(
                db.run {
                  games.delete andThen
                    districts.delete
                },
                20 seconds)

              val future = db.run(checkNoInsertConflict(newEvent))

              Await.ready(future, 20 seconds)
              future.value.get.isFailure mustBe true
              future.failed.map(_ must matchPattern {
                case _: GameNotFoundException =>
              })
            }
          }
        }
      }
      "there is an event with the same name and a different game" when {
        val existingEvent = Event(1, None, 2, "foo", None, None, None)
        val gameForExistingEvent = Game(2, "bar", "FTC", Year.of(2531))
        "the given game exists" when {
          val gameForNewEvent = Game(1, "fooGame", "FRC", Year.of(2017))
          "the given district exists" must {
            "return success" in {
              val districtForNewEvent = District(1, "FOO", "fooDistrict", "FRC", Year.of(2531), None)
              Await.result(
                db.run {
                  (games forceInsert gameForNewEvent) andThen
                    (districts forceInsert districtForNewEvent) andThen
                    (games forceInsert gameForExistingEvent) andThen
                    (events += existingEvent)
                },
                20 seconds
              )

              val future = db.run(checkNoInsertConflict(newEvent))

              Await.ready(future, 20 seconds)
              future.value.get.isSuccess mustBe true
            }
          }
          "the given district does not exist" must {
            "return failure with DistrictNotFoundException" in {
              Await.result(
                db.run {
                  (games forceInsert gameForNewEvent) andThen
                    (games forceInsert gameForExistingEvent) andThen
                    (events += existingEvent)
                },
                20 seconds
              )

              val future = db.run(checkNoInsertConflict(newEvent))

              Await.ready(future, 20 seconds)
              future.value.get.isFailure mustBe true
              future.failed.map(_ must matchPattern {
                case _: DistrictNotFoundException =>
              })
            }
          }
        }
        "the given game does not exist" when {
          "the given district exists" must {
            "return failure with GameNotFoundException" in {
              val districtForNewEvent = District(1, "FOO", "fooDistrict", "FRC", Year.of(2531), None)
              Await.result(
                db.run {
                  games.delete andThen
                    (games forceInsert gameForExistingEvent) andThen
                    (events += existingEvent) andThen
                    (districts forceInsert districtForNewEvent)
                },
                20 seconds
              )

              val future = db.run(checkNoInsertConflict(newEvent))

              Await.ready(future, 20 seconds)
              future.value.get.isFailure mustBe true
              future.failed.map(_ must matchPattern {
                case _: GameNotFoundException =>
              })
            }
          }
          "the given district does not exist" must {
            "return failure" in {
              Await.result(
                db.run {
                  districts.delete andThen
                    games.delete andThen
                    (games forceInsert gameForExistingEvent) andThen
                    (events += existingEvent)

                },
                20 seconds
              )

              val future = db.run(checkNoInsertConflict(newEvent))

              Await.ready(future, 20 seconds)
              future.value.get.isFailure mustBe true
              future.failed.map(_ must matchPattern {
                case _: GameNotFoundException =>
              })
            }
          }
        }
      }
      "there is an event with the same game and a different name" when {
        val existingEvent = Event(1, None, 1, "bar", None, None, None)
        val gameForExistingEvent = Game(1, "bar", "FTC", Year.of(2531))
        "the given district exists" must {
          "return success" in {
            val districtForNewEvent = District(1, "FOO", "fooDistrict", "FRC", Year.of(2531), None)
            Await.result(
              db.run {
                (districts forceInsert districtForNewEvent) andThen
                  (games forceInsert gameForExistingEvent) andThen
                  (events += existingEvent)
              },
              20 seconds
            )

            val future = db.run(checkNoInsertConflict(newEvent))

            Await.ready(future, 20 seconds)
            future.value.get.isSuccess mustBe true
          }
        }
        "the given district does not exist" must {
          "return failure with DistrictNotFoundException" in {
            Await.result(
              db.run {
                (games forceInsert gameForExistingEvent) andThen
                  (events += existingEvent)
              },
              20 seconds
            )

            val future = db.run(checkNoInsertConflict(newEvent))

            Await.ready(future, 20 seconds)
            future.value.get.isFailure mustBe true
            future.failed.map(_ must matchPattern {
              case _: DistrictNotFoundException =>
            })
          }
        }
      }
      "there is an event with the same name and game" when {
        val existingEvent = Event(1, None, 1, "foo", None, None, None)
        val gameForExistingEvent = Game(1, "bar", "FTC", Year.of(2531))
        "the given district exists" must {
          val districtForNewEvent = District(1, "FOO", "fooDistrict", "FRC", Year.of(2531), None)
          "return failure with EventAlreadyExistsException" in {
            val districtForNewEvent = District(1, "FOO", "fooDistrict", "FRC", Year.of(2531), None)
            Await.result(
              db.run {
                (districts forceInsert districtForNewEvent) andThen
                  (games forceInsert gameForExistingEvent) andThen
                  (events += existingEvent)
              },
              20 seconds
            )

            val future = db.run(checkNoInsertConflict(newEvent))

            Await.ready(future, 20 seconds)
            future.value.get.isFailure mustBe true
            future.failed.map(_ must matchPattern {
              case _: EventAlreadyExistsException =>
            })
          }
        }
        "the given district does not exist" must {
          "return failure" in {
            Await.result(
              db.run {
                (games forceInsert gameForExistingEvent) andThen
                  (events += existingEvent)
              },
              20 seconds
            )

            val future = db.run(checkNoInsertConflict(newEvent))

            Await.ready(future, 20 seconds)
            future.value.get.isFailure mustBe true
          }
        }
      }
    }
    "checking an event without a district" when {
      val newEvent = Event(1, None, 1, "foo", None, None, None)
      "there are no events" when {
        "the given game exists" must {
          val gameForNewEvent = Game(1, "fooGame", "FRC", Year.of(2017))
          "return success" in {
            Await.result(
              db.run(games forceInsert gameForNewEvent), 20 seconds)

            val future = db.run(checkNoInsertConflict(newEvent))

            Await.ready(future, 20 seconds)
            future.value.get.isSuccess mustBe true
          }
        }
        "the given game does not exist" must {
          "return failure with GameNotFoundException" in {
            val future = db.run(checkNoInsertConflict(newEvent))

            Await.ready(future, 20 seconds)
            future.value.get.isFailure mustBe true
            future.failed.map(_ must matchPattern {
              case _: GameNotFoundException =>
            })
          }
        }
      }
      "there is an event with the same name and a different game" when {
        val existingEvent = Event(1, None, 2, "foo", None, None, None)
        val gameForExistingEvent = Game(2, "bar", "FTC", Year.of(2531))
        "the given game exists" must {
          val gameForNewEvent = Game(1, "fooGame", "FRC", Year.of(2017))
          "return success" in {
            Await.result(
              db.run {
                (games forceInsert gameForExistingEvent) andThen
                  (events forceInsert existingEvent) andThen
                  (games forceInsert gameForNewEvent)
              },
              20 seconds
            )

            val future = db.run(checkNoInsertConflict(newEvent))

            Await.ready(future, 20 seconds)
            future.value.get.isSuccess mustBe true
          }
        }
        "the given game does not exist" must {
          "return failure with GameNotFoundException" in {
            Await.result(
              db.run {
                (games forceInsert gameForExistingEvent) andThen
                  (events forceInsert existingEvent)
              },
              20 seconds
            )

            val future = db.run(checkNoInsertConflict(newEvent))

            Await.ready(future, 20 seconds)
            future.value.get.isFailure mustBe true
            future.failed.map(_ must matchPattern {
              case _: GameNotFoundException =>
            })
          }
        }
      }
      "there is an event with the same game and a different name" must {
        val existingEvent = Event(1, None, 1, "bar", None, None, None)
        val gameForExistingEvent = Game(1, "bar", "FTC", Year.of(2531))
        "return success" in {
          Await.result(
            db.run {
              (games forceInsert gameForExistingEvent) andThen
                (events forceInsert existingEvent)
            },
            20 seconds
          )

          val future = db.run(checkNoInsertConflict(newEvent))

          Await.ready(future, 20 seconds)
          future.value.get.isSuccess mustBe true
        }
      }
      "there is an event with the same name and game" must {
        val existingEvent = Event(1, None, 1, "foo", None, None, None)
        val gameForExistingEvent = Game(1, "bar", "FTC", Year.of(2531))
        "return failure with EventAlreadyExistsException" in {
          Await.result(
            db.run {
              (games forceInsert gameForExistingEvent) andThen
                (events forceInsert existingEvent)
            },
            20 seconds
          )

          val future = db.run(checkNoInsertConflict(newEvent))

          Await.ready(future, 20 seconds)
          future.value.get.isFailure mustBe true
        }
      }
    }
  }

  "checkNoReplaceConflict" when {
    "checking an event with a district" when {

    }
    "checking an event without a district" when {
      
    }
  }

  "checkNoDeleteConflict" when {
    val event = Event(1, None, 1, "fooEvent", None, None, None)
    val gameForEvent = Game(1, "fooGame", "FRC", Year.of(2017))
    "there are no other events" when {
      "there are no matches in the given event" must {
        "return success" in {
          Await.result(
            db.run {
              (games forceInsert gameForEvent) andThen
                (events forceInsert event)
            },
            20 seconds
          )

          val future = db.run(checkNoDeleteConflict(event.id))

          Await.ready(future, 20 seconds)
          future.value.get.isSuccess mustBe true
        }
      }
      "there is a match in the given event" must {
        val `match` = Match(1, 1, 1, "Qualifying")
        "return failure with MatchesInEventException" in {
          Await.result(
            db.run {
              (games forceInsert gameForEvent) andThen
                (events forceInsert event) andThen
                (matches forceInsert `match`)
            },
            20 seconds
          )

          val future = db.run(checkNoDeleteConflict(event.id))

          Await.ready(future, 20 seconds)
          future.value.get.isFailure mustBe true
          future.failed.map(_ must matchPattern {
            case _: MatchesInEventException =>
          })
        }
      }
    }
    "there is another event" when {
      val otherEvent = Event(2, None, 1, "barEvent", None, None, None)
      "there are no matches in the other event" when {
        "there are no matches in the given event" must {
          "return success" in {
            Await.result(
              db.run {
                (games forceInsert gameForEvent) andThen
                  (events forceInsert event) andThen
                  (events forceInsert otherEvent)
              },
              20 seconds
            )

            val future = db.run(checkNoDeleteConflict(event.id))

            Await.ready(future, 20 seconds)
            future.value.get.isSuccess mustBe true
          }
        }
        "there is a match in the given event" must {
          val `match` = Match(1, 1, 1, "Qualifying")
          "return failure with MatchesInEventException" in {
            Await.result(
              db.run {
                (games forceInsert gameForEvent) andThen
                  (events forceInsert event) andThen
                  (events forceInsert otherEvent) andThen
                  (matches forceInsert `match`)
              },
              20 seconds
            )

            val future = db.run(checkNoDeleteConflict(event.id))

            Await.ready(future, 20 seconds)
            future.value.get.isFailure mustBe true
            future.failed.map(_ must matchPattern {
              case _: MatchesInEventException =>
            })
          }
        }
      }
      "there is a match in the other event" when {
        val matchForOther = Match(2, 2, 1, "Qualifying")
        "there are no matches for the given event" must {
          "return success" in {
            Await.result(
              db.run {
                (games forceInsert gameForEvent) andThen
                  (events forceInsert event) andThen
                  (events forceInsert otherEvent) andThen
                  (matches forceInsert matchForOther)
              },
              20 seconds
            )

            val future = db.run(checkNoDeleteConflict(event.id))

            Await.ready(future, 20 seconds)
            future.value.get.isSuccess mustBe true
          }
        }
        "there is a match in the given event" must {
          val `match` = Match(1, 1, 1, "Qualifying")
          "return failure with MatchesInEventException" in {
            Await.result(
              db.run {
                (games forceInsert gameForEvent) andThen
                  (events forceInsert event) andThen
                  (events forceInsert otherEvent) andThen
                  (matches forceInsert `match`) andThen
                  (matches forceInsert matchForOther)
              },
              20 seconds
            )

            val future = db.run(checkNoDeleteConflict(event.id))

            Await.ready(future, 20 seconds)
            future.value.get.isFailure mustBe true
            future.failed.map(_ must matchPattern {
              case _: MatchesInEventException =>
            })
          }
        }
      }
    }
  }

  before {
    Await.result(
      db.run {
        games.schema.create andThen
          districts.schema.create andThen
          events.schema.create andThen
          matches.schema.create
      },
      20 seconds
    )
  }

  after {
    Await.result(
      db.run {
        matches.schema.drop andThen
        events.schema.drop andThen
          districts.schema.drop andThen
          games.schema.drop

      },
      20 seconds
    )
  }
}
