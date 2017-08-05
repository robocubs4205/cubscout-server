package com.robocubs4205.cubscout.controllers.apiv1

import javax.inject.Inject

import com.robocubs4205.cubscout._
import com.robocubs4205.cubscout.controllers.EtagDoesNotMatchException
import com.robocubs4205.cubscout.model._
import com.robocubs4205.cubscout.service.GameService
import play.Logger
import play.api.libs.json.{JsError, JsSuccess, JsValue, Json}
import play.api.mvc._
import slick.jdbc.TransactionIsolation._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Failure

/**
  * Created by trevor on 7/31/17.
  */
class GameController @Inject()(cc: ControllerComponents, csdb: CubScoutDb, gameService: GameService)
                              (implicit ec: ExecutionContext) extends AbstractController(cc) {

  import csdb._
  import dbConfig._
  import profile.api._

  import gameService._

  val gameEtagWriter = implicitly[EtagWriter[Game]]

  def index(context: Option[String]) = Action.async { implicit request: Request[AnyContent] =>
    implicit val responseCtx = ResponseCtx(context, request.id)
    db.run {
      games.result
    }.map(JsonResponseWrapper(_)).map(Json.toJson(_)).map(Ok(_))
  }

  def get(id: Long, context: Option[String]) = Action.async { implicit request: Request[AnyContent] =>
    implicit val responseCtx = ResponseCtx(context, request.id)
    db.run(findById(id)).flatMap {
      case Some(g) => Future(g).map(JsonResponseWrapper(_)).map(Json.toJson(_)).map(Ok(_))
      case None => Future(JsonErrorResponseWrapper(NOT_FOUND, "game not found")).map(Json.toJson(_)).map(NotFound(_))
    }
  }

  def create(context: Option[String]) = Action.async(parse.json) { implicit request: Request[JsValue] =>
    implicit val responseCtx = ResponseCtx(context, request.id)
    Json.fromJson[JsonSingleRequestWrapper[Game]](request.body).map(_.data).map { game =>
      db.run {
        checkNoInsertConflict(game).andThen(doInsert(game)).transactionally.withTransactionIsolation(RepeatableRead)
      }.flatMap(game => Future(game).map(JsonResponseWrapper(_)).map(Json.toJson(_)).map(Created(_))
        .map(_.withHeaders(
          (ETAG, gameEtagWriter.etag(game)),
          (LOCATION, controllers.apiv1.routes.GameController.get(game.id, None).url)
        ))
      ).recoverWith {
        case e: GameWithSameNameExistsException =>
          Future(e).map(JsonErrorResponseWrapper(CONFLICT, _)).map(Json.toJson(_)).map(Conflict(_))
        case e: GameWithSameYearAndTypeException =>
          Future(e).map(JsonErrorResponseWrapper(CONFLICT, _)).map(Json.toJson(_)).map(Conflict(_))
      }.andThen {
        case Failure(e) => Logger.error("an unexpected exception occurred when creating a District", e)
      }
    } match {
      case e: JsError => Future(e).map(JsonErrorResponseWrapper(_)).map(Json.toJson(_)).map(UnprocessableEntity(_))
      case JsSuccess(r, _) => r
    }
  }

  def replace(id: Long, context: Option[String]) = Action.async(parse.json) { implicit request: Request[JsValue] =>
    implicit val responseCtx = ResponseCtx(context, request.id)
    if (!request.headers.hasHeader(IF_MATCH)) {
      Future(JsonErrorResponseWrapper(428, "an If-Match header is required for modifying a district"))
        .map(Json.toJson(_)).map(Status(428)(_))
    }
    else Json.fromJson[JsonSingleRequestWrapper[Game]](request.body).map(_.data).map { game =>
      db.run {
        checkNoReplaceConflict(game, id).andThen(findById(id)).flatMap {
          case None => DBIO.successful[Option[Game]](None)
          case Some(g) =>
            if (gameEtagWriter.etag(g) != request.headers(IF_MATCH)) DBIO.failed(EtagDoesNotMatchException())
            else doReplace(game, id)
        }.transactionally.withTransactionIsolation(RepeatableRead)
      }.flatMap {
        case None =>
          Future(JsonErrorResponseWrapper(NOT_FOUND, "game not found")).map(Json.toJson(_)).map(NotFound(_))
        case Some(g) => Future(g).map(JsonResponseWrapper(_)).map(Json.toJson(_)).map(Ok(_))
          .map(_.withHeaders((ETAG, gameEtagWriter.etag(g))))
      }.recoverWith {
        case _: EtagDoesNotMatchException => Future(JsonErrorResponseWrapper(PRECONDITION_FAILED,
          "Etag in If-Match header does not match; game has been updated since Etag was retrieved")
        ).map(Json.toJson(_)).map(PreconditionFailed(_))
        case e: GameWithSameNameExistsException =>
          Future(e).map(JsonErrorResponseWrapper(CONFLICT, _)).map(Json.toJson(_)).map(Conflict(_))
      }.andThen {
        case Failure(e) => Logger.error("an unexpected exception occurred when updating a game", e)
      }
    } match {
      case e: JsError => Future(e).map(JsonErrorResponseWrapper(_)).map(Json.toJson(_)).map(UnprocessableEntity(_))
      case JsSuccess(r, _) => r
    }
  }

  def delete(id: Long, context: Option[String]) = Action.async { implicit request: Request[AnyContent] =>
    implicit val responseCtx = ResponseCtx(context, request.id)
    db.run {
      checkNoDeleteConflict(id).andThen(doDelete(id)).transactionally.withTransactionIsolation(Serializable)
    }.flatMap {
      case Some(g) => Future(g).map(JsonResponseWrapper(_)).map(Json.toJson(_)).map(Ok(_))
      case None => Future(JsonErrorResponseWrapper(NOT_FOUND, "game not found")).map(Json.toJson(_)).map(NotFound(_))
    }.recoverWith {
      case e: EventsInGameException =>
        Future(e).map(JsonErrorResponseWrapper(CONFLICT, _)).map(Json.toJson(_)).map(Conflict(_))
      case e: RobotsInGameException =>
        Future(e).map(JsonErrorResponseWrapper(CONFLICT, _)).map(Json.toJson(_)).map(Conflict(_))
    }.andThen {
      case Failure(e) => Logger.error("an unexpected exception occurred when deleting a game", e)
    }
  }

  def eventIndex(id: Long, context: Option[String]) = Action.async { implicit request: Request[AnyContent] =>
    implicit val responseCtx = ResponseCtx(context, request.id)
    db.run {
      events.filter(_.gameId === id).result
    }.map(JsonResponseWrapper(_)).map(Json.toJson(_)).map(Ok(_))
  }

  def robotIndex(id: Long, context: Option[String]) = Action.async { implicit request: Request[AnyContent] =>
    implicit val responseCtx = ResponseCtx(context, request.id)
    db.run {
      robots.filter(_.gameId === id).result
    }.map(JsonResponseWrapper(_)).map(Json.toJson(_)).map(Ok(_))
  }

}
