package com.robocubs4205.cubscout.api.v1.controllers

import javax.inject._

import com.robocubs4205.cubscout.JsonSingleRequestWrapper._
import com.robocubs4205.cubscout._
import com.robocubs4205.cubscout.api.v1.services.DistrictService
import com.robocubs4205.cubscout.model.District._
import com.robocubs4205.cubscout.model._
import play.Logger
import play.api.libs.json._
import play.api.mvc._
import slick.jdbc.TransactionIsolation._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Failure

class DistrictController @Inject()(cc: ControllerComponents, csdb: CubScoutDb, districtService: DistrictService)
                                  (implicit ec: ExecutionContext) extends AbstractController(cc) {

  val districtEtagWriter = implicitly[EtagWriter[District]]

  import csdb._
  import dbConfig._
  import districtService._
  import profile.api._

  def index(context: Option[String]) = Action.async { implicit request: Request[AnyContent] =>
    implicit val responseCtx = ResponseCtx(context, request.id)
    db.run {
      districts.result
    }.map(JsonResponseWrapper(_)).map(Json.toJson(_)).map(Ok(_))
  }

  def get(id: Long, context: Option[String]) = Action.async { implicit request: Request[_] =>
    implicit val responseCtx = ResponseCtx(context, request.id)
    db.run(findById(id)).flatMap {
      case Some(d) => Future(d).map(JsonResponseWrapper(_)).map(Json.toJson(_)).map(Ok(_))
        .map(_.withHeaders((ETAG, districtEtagWriter.etag(d))))
      case None =>
        Future(JsonErrorResponseWrapper(NOT_FOUND, "district not found")).map(Json.toJson(_)).map(NotFound(_))
    }
  }

  def create(context: Option[String]) = Action.async(parse.json) { implicit request: Request[JsValue] =>
    implicit val responseCtx = ResponseCtx(context, request.id)
    Json.fromJson[JsonSingleRequestWrapper[District]](request.body).map(_.data).map { district =>
      db.run {
        checkNoInsertConflict(district).andThen(doInsert(district))
          .transactionally.withTransactionIsolation(RepeatableRead)
      }.flatMap(d => Future(d).map(JsonResponseWrapper(_)).map(Json.toJson(_)).map(Created(_))
        .map(_.withHeaders(
          (ETAG, districtEtagWriter.etag(d)),
          (LOCATION, routes.DistrictController.get(d.id, None).url)
        ))
      ).recoverWith {
        case e: DistrictWithCodeExistsException =>
          Future(e).map(JsonErrorResponseWrapper(CONFLICT, _)).map(Json.toJson(_)).map(Conflict(_))
        case e: DistrictWithNameExistsException =>
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
    else Json.fromJson[JsonSingleRequestWrapper[District]](request.body).map(_.data).map { district =>
      db.run {
        checkNoReplaceConflict(district, id).andThen(findById(id)).flatMap {
          case None => DBIO.successful[Option[District]](None)
          case Some(d) =>
            if (districtEtagWriter.etag(d) != request.headers(IF_MATCH)) DBIO.failed(EtagDoesNotMatchException())
            else doReplace(district, id)
        }.transactionally.withTransactionIsolation(RepeatableRead)
      }.flatMap {
        case None =>
          Future(JsonErrorResponseWrapper(NOT_FOUND, "district not found")).map(Json.toJson(_)).map(NotFound(_))
        case Some(d) => Future(d).map(JsonResponseWrapper(_)).map(Json.toJson(_)).map(Ok(_))
          .map(_.withHeaders((ETAG, districtEtagWriter.etag(d))))
      }.recoverWith {
        case _: EtagDoesNotMatchException =>
          Future(JsonErrorResponseWrapper(
            PRECONDITION_FAILED,
            "Etag in If-Match header does not match; district has been updated since Etag was retrieved"
          )).map(Json.toJson(_)).map(PreconditionFailed(_))
      }.andThen {
        case Failure(e) => Logger.error("an unexpected exception occurred when updating a District", e)
      }
    } match {
      case e: JsError => Future(e).map(JsonErrorResponseWrapper(_)).map(Json.toJson(_)).map(UnprocessableEntity(_))
      case JsSuccess(r, _) => r
    }
  }

  def delete(id: Long, context: Option[String]) = Action.async { implicit request: Request[_] =>
    implicit val responseCtx = ResponseCtx(context, request.id)
    db.run {
      checkNoDeleteConflict(id).andThen(doDelete(id)).transactionally.withTransactionIsolation(Serializable)
    }.flatMap {
      case Some(d) => Future(d).map(JsonResponseWrapper(_)).map(Json.toJson(_)).map(Ok(_))
      case None =>
        Future(JsonErrorResponseWrapper(NOT_FOUND, "district not found")).map(Json.toJson(_)).map(NotFound(_))
    }.recoverWith {
      case e: EventsInDistrictException =>
        Future(e).map(JsonErrorResponseWrapper(CONFLICT, _)).map(Json.toJson(_)).map(Conflict(_))
      case e: TeamsInDistrictException =>
        Future(e).map(JsonErrorResponseWrapper(CONFLICT, _)).map(Json.toJson(_)).map(Conflict(_))
    }.andThen {
      case Failure(e) => Logger.error("an unexpected exception occurred when deleting a District", e)
    }
  }

  def eventsIndex(id: Long, context: Option[String]) = Action.async {
    implicit request: Request[_] =>
      implicit val responseCtx = ResponseCtx(context, request.id)
      db.run {
        events.filter(_.districtId === id).result
      }.map(JsonResponseWrapper(_)).map(Json.toJson(_)).map(Ok(_))
  }

  def teamsIndex(id: Long, context: Option[String]) = Action.async {
    implicit request: Request[_] =>
      implicit val responseCtx = ResponseCtx(context, request.id)
      db.run {
        teams.filter(_.districtId === id).result
      }.map(JsonResponseWrapper(_)).map(Json.toJson(_)).map(Ok(_))
  }

  private case class EventsInDistrictException()
    extends RuntimeException("There are events in that district, which prevents its deletion")

  private case class TeamsInDistrictException()
    extends RuntimeException("There are teams in that district, which prevents its deletion")

  private case class DistrictWithCodeExistsException()
    extends RuntimeException("A district with that code already exists")

  private case class DistrictWithNameExistsException()
    extends RuntimeException("A district with that name already exists")

}
