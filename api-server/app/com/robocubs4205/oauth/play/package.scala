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

  implicit val invalidAuthCodeExceptionWrites:Writes[InvalidAuthCodeException.type] =
    _ => Json.obj(
      "error"->"invalid_grant",
      "error_description"->"The grant code was invalid"
    )
}
