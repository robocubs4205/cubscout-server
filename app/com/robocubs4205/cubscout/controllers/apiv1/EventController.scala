package com.robocubs4205.cubscout.controllers.apiv1

import javax.inject.Inject

import com.robocubs4205.cubscout.controllers.{DistrictNotFoundException, EtagDoesNotMatchException, GameNotFoundException}
import com.robocubs4205.cubscout.model._
import com.robocubs4205.cubscout.{CubScoutDb, _}
import play.Logger
import play.api.libs.json._
import play.api.mvc._
import slick.jdbc.TransactionIsolation._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Failure

/**
  * Created by trevor on 7/22/17.
  */
class EventController @Inject()(cc: ControllerComponents, csdb: CubScoutDb)(implicit ec: ExecutionContext)
  extends AbstractController(cc) {

  import csdb._
  import dbConfig._
  import profile.api._

  val eventEtagWriter = implicitly[EtagWriter[Event]]

  def index(context: Option[String]) = Action.async { implicit request: Request[AnyContent] =>
    implicit val responseCtx = ResponseCtx(context, request.id)
    db.run {
      events.result
    }.map(JsonResponseWrapper(_)).map(Json.toJson(_)).map(Ok(_))
  }

  def get(id: Long, context: Option[String]) = Action.async {
    implicit request: Request[_] =>
      implicit val responseCtx = ResponseCtx(context, request.id)
      db.run {
        events.filter(event => event.id === id).result.headOption
      }.flatMap {
        case Some(e) => Future(e).map(JsonResponseWrapper(_)).map(Json.toJson(_)).map(Ok(_))
        case None => Future(JsonErrorResponseWrapper(NOT_FOUND, "event not found")).map(Json.toJson(_)).map(NotFound(_))
      }
  }

  def create(context: Option[String]) = Action.async(parse.json) { implicit request: Request[JsValue] =>
    implicit val responseCtx = ResponseCtx(context, request.id)
    Json.fromJson[JsonSingleRequestWrapper[Event]](request.body).map(_.data).map { implicit event =>
      db.run {
        {
          for (
            game <- games.filter(_.id === event.gameId).result.headOption;
            district <- districts.filter(_.id === event.districtId).result.headOption;
            existingEvent <- events.filter(_.name === event.name)
              .filter(_.gameId === event.gameId).result.headOption
          ) yield if (game.isEmpty) DBIO.failed(GameNotFoundException())
          else if (district.isEmpty) DBIO.failed(DistrictNotFoundException())
          else if (existingEvent.isDefined) DBIO.failed(EventAlreadyExistsException())
          else DBIO.successful(())
        }.flatten.andThen(
          (events returning events.map(e => e.id)
            into ((event, id) => event.copy(id = id))) += event
        ).transactionally.withTransactionIsolation(RepeatableRead)
      }.flatMap(e => Future(e).map(JsonResponseWrapper(_)).map(Json.toJson(_)).map(Created(_))
        .map(_.withHeaders(
          (ETAG, eventEtagWriter.etag(e)),
          (LOCATION, controllers.apiv1.routes.EventController.get(e.id, None).url)
        ))
      ).recoverWith {
        case e: GameNotFoundException =>
          Future(e).map(JsonErrorResponseWrapper(CONFLICT, _)).map(Json.toJson(_)).map(Conflict(_))
        case e: DistrictNotFoundException =>
          Future(e).map(JsonErrorResponseWrapper(CONFLICT, _)).map(Json.toJson(_)).map(Conflict(_))
        case e: EventAlreadyExistsException =>
          Future(e).map(JsonErrorResponseWrapper(CONFLICT, _)).map(Json.toJson(_)).map(Conflict(_))
      }.andThen {
        case Failure(e) => Logger.error("an unexpected exception occurred when creating a District", e)
      }
    } match {
      case e: JsError => Future(e).map(JsonErrorResponseWrapper(_)).map(Json.toJson(_)).map(UnprocessableEntity(_))
      case JsSuccess(r, _) => r
    }
  }

  def put(id: Long, context: Option[String]) = Action.async(parse.json) { implicit request: Request[JsValue] =>
    implicit val responseCtx = ResponseCtx(context, request.id)
    if (!request.headers.hasHeader(IF_MATCH)) {
      Future(JsonErrorResponseWrapper(428, "an If-Match header is required for modifying a district"))
        .map(Json.toJson(_)).map(Status(428)(_))
    }
    else Json.fromJson[JsonSingleRequestWrapper[Event]](request.body).map(_.data).map { event =>
      db.run {
        {
          for {
            existingEvent <- events.filter(_.name === event.name).filter(_.gameId === event.gameId).result.headOption
            game <- games.filter(_.id === event.gameId).result.headOption
            district <- districts.filter(_.id === event.districtId).result.headOption
          } yield if (existingEvent.isDefined) DBIO.failed(EventAlreadyExistsException())
          else if (game.isEmpty) DBIO.failed(GameNotFoundException())
          else if (district.isEmpty && event.districtId.isDefined) DBIO.failed(DistrictNotFoundException())
          else DBIO.successful(())
        }.flatten.andThen(
          events.filter(_.id === id).result.headOption
        ).flatMap {
          case None => DBIO.successful[Option[Event]](None)
          case Some(d) =>
            if (eventEtagWriter.etag(d) != request.headers(IF_MATCH)) DBIO.failed(EtagDoesNotMatchException())
            else events.filter(_.id === id).update(event.copy(id = id)).map(_ => Some(event.copy(id = id)))
        }.transactionally.withTransactionIsolation(Serializable)
      }.flatMap {
        case None =>
          Future(JsonErrorResponseWrapper(NOT_FOUND, "event not found")).map(Json.toJson(_)).map(NotFound(_))
        case Some(e) => Future(e).map(JsonResponseWrapper(_)).map(Json.toJson(_)).map(Ok(_))
          .map(_.withHeaders((ETAG, eventEtagWriter.etag(e))))
      }.recoverWith {
        case _: EtagDoesNotMatchException => Future(JsonErrorResponseWrapper(PRECONDITION_FAILED,
          "Etag in If-Match header does not match; event has been updated since Etag was retrieved")
        ).map(Json.toJson(_)).map(PreconditionFailed(_))
        case e: EventAlreadyExistsException =>
          Future(e).map(JsonErrorResponseWrapper(CONFLICT, _)).map(Json.toJson(_)).map(Conflict(_))
        case e: GameNotFoundException =>
          Future(e).map(JsonErrorResponseWrapper(CONFLICT, _)).map(Json.toJson(_)).map(Conflict(_))
        case e: DistrictNotFoundException =>
          Future(e).map(JsonErrorResponseWrapper(CONFLICT, _)).map(Json.toJson(_)).map(Conflict(_))
      }.andThen {
        case Failure(e) => Logger.error("an unexpected exception occurred when updating an event", e)
      }
    } match {
      case e: JsError => Future(e).map(JsonErrorResponseWrapper(_)).map(Json.toJson(_)).map(UnprocessableEntity(_))
      case JsSuccess(r, _) => r
    }
  }

  def delete(id: Long, context: Option[String]) = Action.async { implicit request: Request[_] =>
    implicit val responseCtx = ResponseCtx(context, request.id)
    db.run {
      {
        for {
          matches <- matches.filter(_.eventId === id).result
        } yield if (matches.nonEmpty) DBIO.failed(MatchesInEventException())
        else DBIO.successful(())
      }.flatten.andThen(
        for {
          event <- events.filter(_.id === id).result.headOption
          _ <- events.filter(_.id === id).delete
        } yield event
      ).transactionally.withTransactionIsolation(Serializable)
    }.flatMap {
      case Some(e) => Future(e).map(JsonResponseWrapper(_)).map(Json.toJson(_)).map(Ok(_))
      case None => Future(JsonErrorResponseWrapper(NOT_FOUND, "event not found")).map(Json.toJson(_)).map(NotFound(_))
    }.recoverWith {
      case e: MatchesInEventException =>
        Future(e).map(JsonErrorResponseWrapper(CONFLICT, _)).map(Json.toJson(_)).map(Conflict(_))
    }.andThen {
      case Failure(e) => Logger.error("an unexpected exception occurred when deleting a game", e)
    }
  }

  def matchIndex(id: Long, context: Option[String]) = Action.async {
    implicit request: Request[_] =>
      implicit val responseCtx = ResponseCtx(context, request.id)
      db.run {
        matches.filter(_.eventId === id).result
      }.map(JsonResponseWrapper(_)).map(Json.toJson(_)).map(Ok(_))
  }

  case class MatchesInEventException()
    extends RuntimeException("there are matches in that event, which prevents its deletion")

  case class EventAlreadyExistsException() extends RuntimeException("an event with that name already exists")

}
