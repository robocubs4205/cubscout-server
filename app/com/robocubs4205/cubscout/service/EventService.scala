package com.robocubs4205.cubscout.service

import javax.inject.Inject

import com.robocubs4205.cubscout.CubScoutDb
import com.robocubs4205.cubscout.controllers.{DistrictNotFoundException, GameNotFoundException}
import com.robocubs4205.cubscout.model.Event

import scala.concurrent.ExecutionContext

class EventService @Inject()(csdb: CubScoutDb)(implicit ec: ExecutionContext) {

  import csdb._
  import dbConfig._
  import profile.api._

  def findById(id: Long) = events.filter(event => event.id === id).result.headOption

  def checkNoInsertConflict(event: Event) = {
    for (
      game <- games.filter(_.id === event.gameId).result.headOption;
      district <- districts.filter(_.id === event.districtId).result.headOption;
      existingEvent <- events.filter(_.name === event.name).filter(_.gameId === event.gameId).result.headOption
    ) yield if (existingEvent.isDefined) DBIO.failed(EventAlreadyExistsException())
    else if (game.isEmpty) DBIO.failed(GameNotFoundException())
    else if (district.isEmpty && event.districtId.isDefined) DBIO.failed(DistrictNotFoundException())
    else DBIO.successful(())
  }.flatten

  def checkNoReplaceConflict(event: Event, id: Long) = {
    for (
      game <- games.filter(_.id === event.gameId).result.headOption;
      district <- districts.filter(_.id === event.districtId).result.headOption;
      existingEvent <- events.filter(_.name === event.name).filter(_.gameId === event.gameId).filter(_.id =!= id)
        .result.headOption
    ) yield if (existingEvent.isDefined) DBIO.failed(EventAlreadyExistsException())
    else if (game.isEmpty) DBIO.failed(GameNotFoundException())
    else if (district.isEmpty && event.districtId.isDefined) DBIO.failed(DistrictNotFoundException())
    else DBIO.successful(())
  }.flatten

  def checkNoDeleteConflict(id: Long) = {
    for {
      matches <- matches.filter(_.eventId === id).result
    } yield if (matches.nonEmpty) DBIO.failed(MatchesInEventException())
    else DBIO.successful(())
  }.flatten

  def doInsert(event: Event) =
    (events returning events.map(e => e.id) into ((event, id) => event.copy(id = id))) += event

  def doReplace(event: Event, id: Long) =
    events.filter(_.id === id).update(event.copy(id = id)).map(_ => Some(event.copy(id = id)))

  def doDelete(id: Long) = for {
    event <- events.filter(_.id === id).result.headOption
    _ <- events.filter(_.id === id).delete
  } yield event

  case class MatchesInEventException()
    extends RuntimeException("there are matches in that event, which prevents its deletion")

  case class EventAlreadyExistsException() extends RuntimeException("an event with that name already exists")

}
