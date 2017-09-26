package com.robocubs4205.oath

import com.netaporter.uri.Uri

/**
  * Created by trevor on 9/16/17.
  */
sealed trait GrantRequest {
  def clientId: String

  def clientSecret: Option[String]

  def grantType: GrantType

  def redirectUri: Uri
}

object GrantRequest {

  case class AuthCodeGrantRequest(
    authCode: String,
    clientId: String,
    clientSecret: Option[String],
    redirectUri: Uri
  ) extends GrantRequest {
    val grantType = GrantType.AuthCode
  }

  case class PasswordGrantRequest(
    username: String,
    password: String,
    scopes: String,
    clientId: String,
    clientSecret: Option[String],
    redirectUri: Uri) extends GrantRequest {
    val grantType = GrantType.Password
  }

}