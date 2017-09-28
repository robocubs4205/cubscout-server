package com.robocubs4205.oauth.play

import com.robocubs4205.oauth._
import _root_.play.api.libs.json._
import _root_.play.api.mvc.{BaseController, Request}
import com.robocubs4205.cubscout.JsonErrorResponseWrapper
import com.robocubs4205.oauth.grant._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

trait OauthComponent {
  self: BaseController =>
  def grantHandler:GrantHandler

  implicit def ec:ExecutionContext

  def handleOauthGrantRequest() = Action.async(parse.json) { implicit request:Request[JsValue]=>
    Json.fromJson[GrantRequest](request.body).map{ grantRequest =>
      grantHandler.handleRequest(grantRequest).map(Json.toJson(_)).map(Ok(_))
    }.recoverTotal{
      case JsError(_) => Future.failed(InvalidRequestException)
    }.recover{
      case v@InvalidAuthCodeException => BadRequest(Json.toJson(v))
      case v@InvalidClientException => Unauthorized(Json.toJson(v))
      case v@InvalidRedirectException => BadRequest(Json.toJson(v))
      case v@InvalidRequestException => BadRequest(Json.toJson(v))
      case v@InvalidScopeException => BadRequest(Json.toJson(v))
      case v@InvalidUserException => BadRequest(Json.toJson(v))
      case v@UnauthorizedGrantTypeException => BadRequest(Json.toJson(v))
      case v@UnsupportedGrantTypeException => BadRequest(Json.toJson(v))
    }.map(_.withHeaders(CACHE_CONTROL->"no-store",PRAGMA->"no-cache"))
  }
}
