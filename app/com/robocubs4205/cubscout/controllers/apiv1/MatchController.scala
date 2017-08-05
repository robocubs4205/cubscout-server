package com.robocubs4205.cubscout.controllers.apiv1

import javax.inject.Inject

import com.robocubs4205.cubscout.controllers._
import com.robocubs4205.cubscout.{CubScoutDb, EtagWriter, JsonErrorResponseWrapper, JsonResponseWrapper, JsonSingleRequestWrapper, ResponseCtx, controllers}
import com.robocubs4205.cubscout.model._
import play.Logger
import play.api.libs.json.{JsError, JsSuccess, JsValue, Json}
import play.api.mvc.{AbstractController, AnyContent, ControllerComponents, Request}
import slick.jdbc.TransactionIsolation.RepeatableRead

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Failure

/**
  * Created by trevor on 8/3/17.
  */
class MatchController @Inject()(cc: ControllerComponents, csdb: CubScoutDb)(implicit ec: ExecutionContext)
  extends AbstractController(cc) {

  import csdb._
  import dbConfig._
  import profile.api._

  val matchEtagWriter = implicitly[EtagWriter[Match]]

  def index(context: Option[String]) = Action.async { implicit request: Request[AnyContent] =>
    implicit val responseCtx = ResponseCtx(context, request.id)
    db.run {
      matches.result
    }.map(JsonResponseWrapper(_)).map(Json.toJson(_)).map(Ok(_))
  }

  def get(id: Long, context: Option[String]) = Action.async { implicit request: Request[AnyContent] =>
    implicit val responseCtx = ResponseCtx(context, request.id)
    db.run {
      matches.filter(_.id === id).result.headOption
    }.flatMap {
      case Some(m) => Future(m).map(JsonResponseWrapper(_)).map(Json.toJson(_)).map(Ok(_))
      case None => Future(JsonErrorResponseWrapper(NOT_FOUND, "game not found")).map(Json.toJson(_)).map(NotFound(_))
    }
  }

  def create(context: Option[String]) = Action.async(parse.json) { implicit request: Request[JsValue] =>
    implicit val responseCtx = ResponseCtx(context, request.id)
    Json.fromJson[JsonSingleRequestWrapper[Match]](request.body).map(_.data).map { `match` =>
      db.run {
        {
          for{
            event <- events.filter(_.id === `match`.eventId).result.headOption
            existingMatch <- matches.filter(_.eventId === `match`.eventId).filter(_.number === `match`.number)
              .filter(_.`type` === `match`.`type`).result.headOption
          } yield if(event.isEmpty) DBIO.failed(EventNotFoundException())
          else if(existingMatch.isDefined) DBIO.failed(MatchAlreadyExistsException())
          else DBIO.successful(())
        }.flatten.andThen{
          (matches returning matches.map(_.id) into ((`match`, id) => `match`.copy(id = id))) += `match`
        }.transactionally.withTransactionIsolation(RepeatableRead)
      }.flatMap(`match` => Future(`match`).map(JsonResponseWrapper(_)).map(Json.toJson(_)).map(Created(_))
        .map(_.withHeaders(
          (ETAG, matchEtagWriter.etag(`match`)),
          (LOCATION, controllers.apiv1.routes.GameController.get(`match`.id, None).url)
        ))
      ).recoverWith{
        case e:EventNotFoundException =>
          Future(e).map(JsonErrorResponseWrapper(CONFLICT,_)).map(Json.toJson(_)).map(Conflict(_))
        case e:MatchAlreadyExistsException =>
          Future(e).map(JsonErrorResponseWrapper(CONFLICT,_)).map(Json.toJson(_)).map(Conflict(_))
      }.andThen {
        case Failure(e) => Logger.error("an unexpected exception occurred when creating a match", e)
      }
    } match {
      case e: JsError => Future(e).map(JsonErrorResponseWrapper(_)).map(Json.toJson(_)).map(UnprocessableEntity(_))
      case JsSuccess(r, _) => r
    }
  }

  def put(id: Long, context: Option[String]) = Action.async(parse.json) { implicit request: Request[JsValue] =>
    implicit val responseCtx = ResponseCtx(context, request.id)
    if (!request.headers.hasHeader(IF_MATCH)) {
      Future(JsonErrorResponseWrapper(428, "an If-Match header is required for modifying a match"))
        .map(Json.toJson(_)).map(Status(428)(_))
    }
    else Json.fromJson[JsonSingleRequestWrapper[Match]](request.body).map(_.data).map { `match` =>
      db.run {
        {
          for{
            event <- events.filter(_.id === `match`.eventId).result.headOption
            existingMatch <- matches.filter(_.eventId === `match`.eventId).filter(_.number === `match`.number)
              .filter(_.`type` === `match`.`type`).result.headOption
          } yield if(event.isEmpty) DBIO.failed(EventNotFoundException())
          else if(existingMatch.isDefined) DBIO.failed(MatchAlreadyExistsException())
          else DBIO.successful(())
        }.flatten.andThen {
          matches.filter(_.id === `match`.id).result.headOption
        }.flatMap {
          case None => DBIO.successful[Option[Match]](None)
          case Some(m) =>
            if (matchEtagWriter.etag(m) != request.headers(IF_MATCH)) DBIO.failed(EtagDoesNotMatchException())
            else {
              matches.filter(_.id === id).map(m => (m.eventId, m.`type`, m.number))
                .update(`match`.eventId, `match`.`type`, `match`.number)
            }.map(_ => Some(`match`.copy(id = id)))
        }
      }.flatMap {
        case None =>
          Future(JsonErrorResponseWrapper(NOT_FOUND, "match not found")).map(Json.toJson(_)).map(NotFound(_))
        case Some(g) => Future(g).map(JsonResponseWrapper(_)).map(Json.toJson(_)).map(Ok(_))
          .map(_.withHeaders((ETAG, matchEtagWriter.etag(g))))
      }.recoverWith {
        case _: EtagDoesNotMatchException => Future(JsonErrorResponseWrapper(PRECONDITION_FAILED,
          "Etag in If-Match header does not match; match has been updated since Etag was retrieved")
        ).map(Json.toJson(_)).map(PreconditionFailed(_))
        case e:EventNotFoundException =>
          Future(e).map(JsonErrorResponseWrapper(CONFLICT,_)).map(Json.toJson(_)).map(Conflict(_))
        case e:MatchAlreadyExistsException =>
          Future(e).map(JsonErrorResponseWrapper(CONFLICT,_)).map(Json.toJson(_)).map(Conflict(_))
      }.andThen {
        case Failure(e) => Logger.error("an unexpected exception occurred when updating a match", e)
      }
    } match {
      case e: JsError => Future(e).map(JsonErrorResponseWrapper(_)).map(Json.toJson(_)).map(UnprocessableEntity(_))
      case JsSuccess(r, _) => r
    }
  }

  def delete(id:Long,context:Option[String]) = Action.async{implicit request: Request[AnyContent] =>
    implicit val responseCtx = ResponseCtx(context, request.id)
    db.run{
      {
        for{
          results <- results.filter(_.matchId === id).result
        } yield if(results.nonEmpty) DBIO.failed(ResultsInMatchException())
        else DBIO.successful(())
      }.flatten.andThen{
        for{
          _match <- matches.filter(_.id===id).result.headOption
          _ <- matches.filter(_.id===id).delete
        } yield _match
      }
    }.flatMap{
      case None=>
        Future(JsonErrorResponseWrapper(NOT_FOUND, "match not found")).map(Json.toJson(_)).map(NotFound(_))
      case Some(m)=>
        Future(m).map(JsonResponseWrapper(_)).map(Json.toJson(_)).map(Ok(_))
    }.recoverWith{
      case e:ResultsInMatchException =>
        Future(e).map(JsonErrorResponseWrapper(CONFLICT,_)).map(Json.toJson(_)).map(Conflict(_))
    }.andThen {
      case Failure(e) => Logger.error("an unexpected exception occurred when updating a match", e)
    }
  }

  case class MatchAlreadyExistsException() extends RuntimeException("That match already exists")

  case class ResultsInMatchException()
    extends RuntimeException("There are results for that match, which prevents its deletion")
}
