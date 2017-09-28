package com.robocubs4205.oauth

import _root_.play.api.libs.json._
import _root_.play.api.libs.functional.syntax._
import com.netaporter.uri.Uri

import GrantRequest._

package object play {
  implicit val authCodeGrantRequestReads:Reads[AuthCodeGrantRequest] = (
    (JsPath\"auth_code").read[String] and
      (JsPath\"client_id").read[String] and
      (JsPath\"client_secret").read[Option[String]] and
      (JsPath\"redirect_uri").read[Uri]
  )(AuthCodeGrantRequest.apply,unlift(AuthCodeGrantRequest.unapply))

  implicit val passwordGrantRequestReads:Reads[PasswordGrantRequest] = (
    (JsPath\"username").read[String] and
      (JsPath\"password").read[String] and
      (JsPath\"scopes").read[Seq[String]] and
      (JsPath\"client_id").read[String] and
      (JsPath\"client_secret").read[Option[String]] and
      (JsPath\"redirect_uri").read[Uri]
  )(PasswordGrantRequest.apply,unlift(PasswordGrantRequest.unapply))

  implicit val grantRequestReads:Reads[GrantRequest] =
    authCodeGrantRequestReads.map(_.asInstanceOf[GrantRequest]) orElse
      passwordGrantRequestReads.map(_.asInstanceOf[GrantRequest])

  val ERROR = "error"
  val ERROR_DESCRIPTION = "error_description"
  val INVALID_GRANT = "invalid_grant"
  val INVALID_CLIENT = "invalid_client"
  val INVALID_REQUEST = "invalid_request"
  val INVALID_SCOPE = "invalid_scope"
  val UNAUTHORIZED_CLIENT = "unauthorized_client"
  val UNSUPPORTED_GRANT_TYPE = "unsupported_grant_type"
  
  implicit val invalidAuthCodeExceptionWrites:Writes[InvalidAuthCodeException.type] =
    _ => Json.obj(
      ERROR->INVALID_GRANT,
      ERROR_DESCRIPTION->"The grant code was invalid"
    )

  implicit val invalidClientExceptionWrites:Writes[InvalidClientException.type] =
    _ => Json.obj(
      ERROR->INVALID_CLIENT,
      ERROR_DESCRIPTION->"The client credentials were invalid"
    )

  implicit val invalidRedirectExceptionWrites:Writes[InvalidRedirectException.type] =
    _ => Json.obj(
      ERROR->INVALID_GRANT,
      ERROR_DESCRIPTION->
        "The redirect URL was unregistered or did not match that given when requesting the AuthCode (if used)"
    )

  implicit val invalidRequestExceptionWrites:Writes[InvalidRequestException.type] =
    _ => Json.obj(
      ERROR->INVALID_REQUEST,
      ERROR_DESCRIPTION->"The request was missing a required parameter"
    )

  implicit val invalidScopeExceptionWrites:Writes[InvalidScopeException.type] =
    _ => Json.obj(
      ERROR->INVALID_SCOPE,
      ERROR_DESCRIPTION->"An invalid scope was requested"
    )

  implicit val invalidUserExceptionWrites:Writes[InvalidUserException.type] =
    _ => Json.obj(
      ERROR->INVALID_GRANT,
      ERROR_DESCRIPTION->"The user's credentials were invalid"
    )

  implicit val unauthorizedGrantTypeExceptionWrites:Writes[UnauthorizedGrantTypeException.type] =
    _ => Json.obj(
      ERROR->UNAUTHORIZED_CLIENT,
      ERROR_DESCRIPTION->"The client is not authorized to use the given grant type"
    )

  implicit val unsupportedGrantTypeException:Writes[UnsupportedGrantTypeException.type] =
    _ => Json.obj(
      ERROR->UNSUPPORTED_GRANT_TYPE,
      ERROR_DESCRIPTION->"The given grant type is not supported"
    )
}
