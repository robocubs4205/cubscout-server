package com.robocubs4205.cubscout.services

import javax.inject.Inject

import com.robocubs4205.cubscout.CubScoutDb
import com.robocubs4205.cubscout.controllers.EventNotFoundException
import com.robocubs4205.cubscout.model.Match

import scala.concurrent.ExecutionContext

/**
  * Created by trevor on 8/5/17.
  */
class MatchService @Inject()(csdb: CubScoutDb)(implicit ec: ExecutionContext) {

  import csdb._
  import dbConfig._
  import profile.api._

  def findById(id: Long) = matches.filter(_.id === id).result.headOption

  def doInsert(`match`: Match) =
    (matches returning matches.map(_.id) into ((`match`, id) => `match`.copy(id = id))) += `match`

  def doReplace(`match`: Match, id: Long) =
    matches.filter(_.id === id).update(`match`.copy(id = id)).map(_ => Some(`match`.copy(id = id)))

  def doDelete(id: Long) = for {
    _match <- findById(id)
    _ <- matches.filter(_.id === id).delete
  } yield _match

  def checkNoInsertConflict(`match`: Match) = {
    for {
      event <- events.filter(_.id === `match`.eventId).result.headOption
      existingMatch <- matches.filter(_.eventId === `match`.eventId).filter(_.number === `match`.number)
        .filter(_.`type` === `match`.`type`).result.headOption
    } yield if (event.isEmpty) DBIO.failed(EventNotFoundException())
    else if (existingMatch.isDefined) DBIO.failed(MatchAlreadyExistsException())
    else DBIO.successful(())
  }.flatten

  def checkNoReplaceConflict(`match`:Match,id:Long) = {
    for{
      event <- events.filter(_.id === `match`.eventId).result.headOption
      existingMatch <- matches.filter(_.eventId === `match`.eventId).filter(_.number === `match`.number)
        .filter(_.`type` === `match`.`type`).filter(_.id=!=id).result.headOption
    } yield if(event.isEmpty) DBIO.failed(EventNotFoundException())
    else if(existingMatch.isDefined) DBIO.failed(MatchAlreadyExistsException())
    else DBIO.successful(())
  }.flatten

  def checkNoDeleteConflict(id:Long) = {
    for{
      results <- results.filter(_.matchId === id).result
    } yield if(results.nonEmpty) DBIO.failed(ResultsInMatchException())
    else DBIO.successful(())
  }.flatten

  case class MatchAlreadyExistsException() extends RuntimeException("That match already exists")

  case class ResultsInMatchException()
    extends RuntimeException("There are results for that match, which prevents its deletion")
}
